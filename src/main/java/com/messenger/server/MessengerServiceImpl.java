package com.messenger.server;

import com.messenger.db.UserDAO;
import com.messenger.models.Status;
import com.messenger.proto.*;
import io.grpc.stub.StreamObserver;
import com.messenger.models.User;
import com.messenger.models.Message;
import com.messenger.db.MessageDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Properties;

/**
 * Created by mamahendru on 4/8/17.
 */
@Service
public class MessengerServiceImpl extends MessengerServiceGrpc.MessengerServiceImplBase {
    @Autowired
    @Qualifier("configEnv")
    protected Properties configEnv;
    @Autowired
    MessageDAO messageDAO;
    @Autowired
    UserDAO userDAO;
    /**
    * map contains messages for each user.
    * users contains all the registered users.
    * I have used concurrenthashmap for both to achieve high concurrency as well as thread safety
     */
    //TODO users need to be stored in permanent storage like mongodb.
    //TODO loggedin users data can be kept in distributed cache like teracotta etc.
    //TODO users logging in from multiple client sessions not implemented currently.
    //TODO - login method should connect to directory server and authenticate user
    //TODO send should spun a new thread just like receive
    //TODO receive should use ExecuterService to ensure threads do not go out of control.
    private Map<String, User> users = new ConcurrentHashMap<String, User>();

    /**
     *
     * @param request
     * @param chatObserver
     * This is receive method which client calls repeatedly.
     * Client sends stream observer and reads messages until server is done.
     * I am starting new receiver thread so as to improve through put.
     * This should be done in ExecutorService so threads do not go out of control.
     */
    @Override
    public void receive(ReceiveRequest request, StreamObserver<ChatMessage> chatObserver) {
        ReceiverMongoDBThread receiverThread = new ReceiverMongoDBThread(chatObserver, request.getUserid(), request.getSessionid());
        new Thread(receiverThread).start();
    }

    /**
     *
     * @param chatMessage
     * @param client
     * Sends the message to requested user.
     * I should check the friends list to ensure that user can send messages to only their friends.
     * This should also spun a new thread so as to not block incoming requests.
     */
    @Override
    public void send (ChatMessage chatMessage, final StreamObserver<Response> client) {
        Response res;
        try {
            if (isValidSession(chatMessage.getFrom(), chatMessage.getSessionid())) {
                User user = users.get(chatMessage.getFrom());
                Message message = new Message(chatMessage.getTo(), chatMessage.getFrom(), chatMessage.getMessage());
                messageDAO.addMessage(message);
            }
            res = Response.newBuilder().setMessage("ok").build();
        }catch(Exception ex){
            //user needs to know if exception occured.
            res = Response.newBuilder().setMessage("Couldnot save message").build();
        }
        client.onNext(res);
        client.onCompleted();
    }

    /**
     *
     * @param request
     * @param streamObserver
     * Registers the user. To be called only once.
     */
    @Override
    public void register(RegisterRequest request, final StreamObserver<Response> streamObserver) {
        User existingUser = users.get(request.getUserid());
        Response res;
        try {
            if (existingUser != null) {
                res = Response.newBuilder().setMessage("User already registered").build();
            } else {
                User user = new User(request.getUserid(), request.getPassword(), request.getFirstname(), request.getLastname());
                users.put(request.getUserid(), user);
                res = Response.newBuilder().setMessage("Welcome").build();
            }
        } catch(Exception ex) {
            res = Response.newBuilder().setMessage("Failed to register").build();
        }
        streamObserver.onNext(res);
        streamObserver.onCompleted();
    }

    /**
     *
     * @param request
     * @param streamObserver
     * login. sets sessionid.
     */
    @Override
    public void login(LoginRequest request, final StreamObserver<Response> streamObserver) {
        User user = users.get(request.getUserid());
        user.setStatus(Status.ACTIVE);
        user.setSessionId(UUID.randomUUID().toString());
        Response res = Response.newBuilder().setMessage(user.getSessionId()).build();
        streamObserver.onNext(res);
        streamObserver.onCompleted();

    }

    /**
     *
     * @param request
     * @param observer
     * add list to contact list.
     */
    @Override
    public void addFriend(FriendRequest request, final StreamObserver<Empty> observer) {
        if(isValidSession(request.getUser(), request.getSessionid())) {
            User user = users.get(request.getUser());
            User friend = users.get(request.getFriend());
            if (user != null && friend != null) {
                user.addFriend(friend);
            }
        }
        observer.onNext(Empty.newBuilder().build());
        observer.onCompleted();
    }

    /**
     *
     * @param request
     * @param observer
     * remove friend from contact list.
     */
    @Override
    public void removeFriend(FriendRequest request, final StreamObserver<Empty> observer) {
        if(isValidSession(request.getUser(), request.getSessionid())) {
            User user = users.get(request.getUser());
            User friend = users.get(request.getFriend());
            if (user != null && friend != null) {
                user.removeFriend(friend);
            }
        }
        observer.onNext(Empty.newBuilder().build());
        observer.onCompleted();
    }

    /**
     *
     * @param request
     * @param client
     * Resets the sessionid. After this user cannot send message/receive message/update contact list.
     */
    @Override
    public void logout(Request request, final StreamObserver<Empty> client) {
        User user = users.get(request.getNickname());
        user.setStatus(Status.SIGNEDOUT);
        user.setSessionId(null);
        client.onNext(Empty.newBuilder().build());
        client.onCompleted();
    }

    /**
     *
     * @param request
     * @param observer
     * Returns user's contact list with status of each user in contact list.
     */

    public void contacts (Request request, final StreamObserver<Response> observer) {
        User user = users.get(request.getNickname());
        if(isValidSession(request.getNickname(), request.getSessionid())){
            for(User u : user.getFriends()){
                Response res = Response.newBuilder().setMessage(u.getUserId() + ":" + u.getStatus()).build();
                observer.onNext(res);
            }
        }
        observer.onCompleted();
    }

    /**
     * User has sessionid which is set during login/register. It is unset during logout.
     * Non null sessionid means user is logged in.
     * This session id is sent to client from where user is logged in.
     * Each client session sends this back to server.
     * isValidSession determines if user client session claims is the correct one.
    */
    private boolean isValidSession(String userid, String sessionid) {
        User user = users.get(userid);
        if(user != null && user.getSessionId() !=null && user.getSessionId().equalsIgnoreCase(sessionid)){
            return true;
        }
        return false;
    }

    /**
     * ReceiverThread to handle the sending of messages to client session.
     * This thread gets the messages saved in mongodb.
     */
    private class ReceiverMongoDBThread implements Runnable {
        StreamObserver<ChatMessage> chatObserver = null;
        String name = null;
        String sessionid = null;
        public ReceiverMongoDBThread(StreamObserver<ChatMessage> chatObserver, String name, String sessionid) {
            this.chatObserver = chatObserver;
            this.name = name;
            this.sessionid = sessionid;
        }
        public void run() {
            System.out.println("messaged for : " + this.name);
            User user = users.get(name);
            if(isValidSession(this.name, this.sessionid)) {
                List<Message> messages = messageDAO.getMessages(this.name);
                if (messages != null) {
                    for(Message msg : messages){
                        ChatMessage chatMessage = ChatMessage.newBuilder().setTo(msg.getTo()).setFrom(msg.getFrom()).setMessage(msg.getMessage()).build();
                        chatObserver.onNext(chatMessage);
                    }
                }
            }
            chatObserver.onCompleted();
        }
    }
}
