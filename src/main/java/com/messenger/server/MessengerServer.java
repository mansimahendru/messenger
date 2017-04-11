package com.messenger.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by mamahendru on 4/8/17.
 * This is the MessengerServer which listens on port 50051 and exposes interfaces implemented by MessengerServiceImpl.
 * Port should be configurable value.
 */
public class MessengerServer {

    protected static final String CONFIG_PATH = "classpath*:spring-config.xml";

    private Server server;

    public static void main(String[] args) throws IOException, InterruptedException{
        System.out.println("Starting server");
        final MessengerServer server = new MessengerServer();
        server.start();
        server.blockUntilShutdown();
    }

    private void start() throws IOException {
        final ApplicationContext context = new ClassPathXmlApplicationContext(CONFIG_PATH);
        MessengerServiceImpl service = context.getBean(MessengerServiceImpl.class);
        int port = Integer.parseInt(service.configEnv.getProperty("server.port"));
        server = ServerBuilder.forPort(port)
                .addService(service)
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
