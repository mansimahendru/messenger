package com.messenger.server;

import com.messenger.proto.Request;
import com.messenger.proto.MessengerServiceGrpc;
import com.messenger.proto.Response;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;

/**
 * Created by mamahendru on 4/8/17.
 * This is the MessengerServer which listens on port 50051 and exposes interfaces implemented by MessengerServiceImpl.
 * Port should be configurable value.
 */
public class MessengerServer {

    private Server server;

    public static void main(String[] args) throws IOException, InterruptedException{
        System.out.println("Starting server");
        final MessengerServer server = new MessengerServer();
        server.start();
        server.blockUntilShutdown();
    }

    private void start() throws IOException {
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new MessengerServiceImpl())
                .build()
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("Shutting down server");
                MessengerServer.this.stop();
                System.err.println("Server shutdown complete");
            }
        });
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}
