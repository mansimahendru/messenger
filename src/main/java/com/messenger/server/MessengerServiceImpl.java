package com.messenger.server;

import com.messenger.models.Status;
import com.messenger.proto.*;
import io.grpc.stub.StreamObserver;
import com.messenger.models.User;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mamahendru on 4/8/17.
 */
public class MessengerServiceImpl extends MessengerServiceGrpc.MessengerServiceImplBase {
    /**
    * map contains messages for each user.
    * users contains all the registered users.
    * I have used concurrenthashmap for both to achieve high concurrency as well as thread safety
     */
    //TODO ideally contents of map and users should be saved in permanenet storage like mongodb.
    //TODO loggedin users data can be kept in distributed cache like teracotta etc.
    //TODO users logging in from multiple client sessions not implemented currently.
    //TODO - login method should connect to directory server and authenticate user
    //TODO send should spun a new thread just like receive
    //TODO receive should use ExecuterService to ensure threads do not go out of control.
    private Map<String, List<ChatMessage>> map = new ConcurrentHashMap<String, List<ChatMessage>>();
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
        ReceiverThread receiverThread = new ReceiverThread(chatObserver, request.getUserid(), request.getSessionid());
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
    public void send (ChatMessage chatMessage, final StreamObserver<Empty> client) {
        if(isValidSession(chatMessage.getFrom(), chatMessage.getSessionid())) {
            User user = users.get(chatMessage.getFrom());
            List<ChatMessage> list = map.get(chatMessage.getTo());
            if (list == null) {
                list = new ArrayList<ChatMessage>();
            }
            list.add(chatMessage);
            map.put(chatMessage.getTo(), list);
        }
        client.onNext(Empty.newBuilder().build());
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
        if(existingUser != null) {
            res = Response.newBuilder().setMessage("User already registered").build();
        }
        else {
            User user = new User(request.getUserid(), request.getPassword(), request.getFirstname(), request.getLastname());
            users.put(request.getUserid(), user);
            res = Response.newBuilder().setMessage("Welcome").build();
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
     * ReceiverThread to handle the sending of messages to client session
     */
    private class ReceiverThread implements Runnable {
        StreamObserver<ChatMessage> chatObserver = null;
        String name = null;
        String sessionid = null;
        public ReceiverThread(StreamObserver<ChatMessage> chatObserver, String name, String sessionid) {
            this.chatObserver = chatObserver;
            this.name = name;
            this.sessionid = sessionid;
        }
        public void run() {
            System.out.println("messaged for : " + this.name);
            User user = users.get(name);
            if(isValidSession(this.name, this.sessionid)) {
                List<ChatMessage> messages = map.get(this.name);
                if (messages != null) {
                    for (ChatMessage msg : messages) {
                        chatObserver.onNext(msg);
                    }
                    messages.clear();
                }
            }
            chatObserver.onCompleted();
        }
    }
}
