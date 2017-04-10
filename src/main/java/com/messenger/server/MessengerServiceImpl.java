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
    //TODO map contains messages for each user.
    //TODO users contains all the registered users.
    //TODO I have used concurrenthashmap for both to achieve high concurrency as well as thread safety.
    //TODO ideally contents of map and users should be saved in permanenet storage like mongodb.
    //TODO loggedin users data can be kept in distributed cache like teracotta etc.
    private Map<String, List<ChatMessage>> map = new ConcurrentHashMap<String, List<ChatMessage>>();
    private Map<String, User> users = new ConcurrentHashMap<String, User>();
    @Override
    public void receive(ReceiveRequest request, StreamObserver<ChatMessage> chatObserver) {
        ReceiverThread receiverThread = new ReceiverThread(chatObserver, request.getUserid(), request.getSessionid());
        new Thread(receiverThread).start();
    }
    @Override
    public void send (ChatMessage chatMessage, final StreamObserver<Empty> client) {
        if(isValidSession(chatMessage.getFrom(), chatMessage.getSessionid())) {
            User user = users.get(chatMessage.getFrom());
            List<ChatMessage> list = map.get(chatMessage.getTo());
            if (list == null) {
                list = new ArrayList<ChatMessage>();
            }
            list.add(chatMessage);
            System.out.println("message added for : " + chatMessage.getTo());
            map.put(chatMessage.getTo(), list);
        }
        client.onNext(Empty.newBuilder().build());
        client.onCompleted();
    }
    @Override
    public void register(RegisterRequest request, final StreamObserver<Response> streamObserver) {
        User user = new User(request.getUserid(), request.getPassword(), request.getFirstname(), request.getLastname());
        users.put(request.getUserid(), user);
        Response res = Response.newBuilder().setMessage("Welcome").build();
        streamObserver.onNext(res);
        streamObserver.onCompleted();
    }
    @Override
    public void login(LoginRequest request, final StreamObserver<Response> streamObserver) {
        //TODO - login method should connect to directory server and authenticate user
        User user = users.get(request.getUserid());
        user.setStatus(Status.ACTIVE);
        user.setSessionId(UUID.randomUUID().toString());
        Response res = Response.newBuilder().setMessage(user.getSessionId()).build();
        streamObserver.onNext(res);
        streamObserver.onCompleted();

    }
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
    @Override
    public void logout(Request request, final StreamObserver<Empty> client) {
        User user = users.get(request.getNickname());
        user.setStatus(Status.SIGNEDOUT);
        user.setSessionId(null);
        client.onNext(Empty.newBuilder().build());
        client.onCompleted();
    }

    private boolean isValidSession(String userid, String sessionid) {
        User user = users.get(userid);
        if(user != null && user.getSessionId() !=null && user.getSessionId().equalsIgnoreCase(sessionid)){
            return true;
        }
        return false;
    }

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
