package com.piggy.microservice.notification.grpc.server;

import com.piggy.microservice.notification.service.RecipientServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationGrpcServerConfiguration {
    private final Server server;
    private final RecipientServiceImpl recipientServiceImpl;

    public NotificationGrpcServerConfiguration(@Value("${grpc.server.port:9092}")int port, RecipientServiceImpl recipientServiceImpl) {
//        System.out.println("AuthService injected: " + (userService != null));
        ServerBuilder<?> builder = ServerBuilder.forPort(port);
        builder.addService(new NotificationGrpcServiceImpl(recipientServiceImpl));
        this.server = builder.build();
        this.recipientServiceImpl = recipientServiceImpl;
    }

    @PostConstruct
    public void start() {
        try {
            System.out.println("Attempting to start gRPC server...");
            server.start();
            System.out.println("Notification gRPC Server started, listening on " + server.getPort());
        } catch (Exception e) {
            System.err.println("Failed to start gRPC server: " + e.getMessage());
            throw new IllegalStateException("Not started", e);
        }
    }

    @PreDestroy
    public void stop() {
        System.out.println("Account gRPC Server stopped");
        server.shutdown();
    }
}
