package com.messenger.client;

import com.messenger.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import sun.rmi.runtime.Log;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.Scanner;

/**
 * Created by mamahendru on 4/8/17.
 */
public class MessengerClient {
    private final ManagedChannel channel;
    private final MessengerServiceGrpc.MessengerServiceBlockingStub sendStub;
    private final MessengerServiceGrpc.MessengerServiceBlockingStub receiverStub;
    private final MessengerServiceGrpc.MessengerServiceBlockingStub loginStub;
    private final MessengerServiceGrpc.MessengerServiceBlockingStub registerStub;
    private final String userid;
    private String sessionid = null;

    public MessengerClient(String name) {
        this.userid = name;
        String host = "localhost";
        int port = 50051;
        ManagedChannelBuilder channelBuilder = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true);
        channel = channelBuilder.build();
        sendStub = MessengerServiceGrpc.newBlockingStub(channel);
        receiverStub = MessengerServiceGrpc.newBlockingStub(channel);
        loginStub = MessengerServiceGrpc.newBlockingStub(channel);
        registerStub = MessengerServiceGrpc.newBlockingStub(channel);;
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }


    public void receive(String nickname, String sessionid) {
        ReceiveRequest receiveRequest = ReceiveRequest.newBuilder().setUserid(nickname).setSessionid(sessionid).build();
        try{
            Iterator<ChatMessage> iterator = receiverStub.receive(receiveRequest);
            while (iterator.hasNext()) {
                ChatMessage msg = iterator.next();
                System.out.println(msg.getFrom() + " : " + msg.getMessage());
            }
        }
        catch(StatusRuntimeException sre) {
            sre.printStackTrace();
        }
    }

    public void send (String name, String message) {
        ChatMessage chatMessage = ChatMessage.newBuilder().setMessage(message).setTo(name).setFrom(this.userid).setSessionid(this.sessionid).build();
        sendStub.send(chatMessage);
    }

    public void login (String userid, String password) {
        LoginRequest loginRequest = LoginRequest.newBuilder().setUserid(userid).setPassword(password).build();
        Response res = loginStub.login(loginRequest);
        this.sessionid = res.getMessage();
    }

    public void register (String userid) {
        RegisterRequest request = RegisterRequest.newBuilder().setUserid(userid).setPassword("password123").setFirstname(userid).setLastname("anything").build();
        Response res = registerStub.register(request);
        System.out.println(res.getMessage());
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting client");
        MessengerClient client = null;
        try {
            String userid = "";
            if (args!= null && args.length > 0) {
                userid = args[0];
            }
            else {
                System.out.println("Command line argument for username needed");
                System.exit(1);
            }
            client = new MessengerClient(userid);
            client.register(userid);
            client.login(userid, "password123");
            Receiver receiver = new Receiver(client);
            Thread receiverThread = new Thread(receiver);
            receiverThread.start();
            Scanner scanner = new Scanner(System.in).useDelimiter("\n");
            while(true) {
                if(scanner.hasNext()) {
                    String msg = scanner.next();
                    if (msg.equalsIgnoreCase("bye")) {
                        receiverThread.interrupt();
                        break;
                    }
                    String[] parts = msg.split(":");
                    client.send(parts[0], parts[1]);
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            client.shutdown();
        }
    }

    private static class Receiver implements Runnable {
        MessengerClient client = null;
        public Receiver(MessengerClient client) {
            this.client = client;
        }
        public void run() {
            while(true) {
                if(!Thread.currentThread().isInterrupted()) {
                    this.client.receive(this.client.userid, this.client.sessionid);
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    return;
                }
            }
        }
    }

}