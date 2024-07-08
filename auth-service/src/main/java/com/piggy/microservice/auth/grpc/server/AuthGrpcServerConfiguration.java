package com.piggy.microservice.auth.grpc.server;

import com.piggy.microservice.auth.repository.UserRepository;
import com.piggy.microservice.auth.service.UserService;
import com.piggy.microservice.auth.grpc.server.UserGrpcServiceImpl;
import com.piggy.microservice.auth.service.UserServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class AuthGrpcServerConfiguration {
    private final Server server;
    private final UserServiceImpl userService;


    public AuthGrpcServerConfiguration(@Value("${grpc.server.port:9091}")int port, UserServiceImpl userService) {
        this.userService = userService;
        System.out.println("AuthService injected: " + (userService != null));
        ServerBuilder<?> builder = ServerBuilder.forPort(port);
        builder.addService(new UserGrpcServiceImpl(userService));
//                .intercept(new AuthInterceptor());
        this.server = builder.build();
    }

    @PostConstruct
    public void start() {
        try {
            System.out.println("Attempting to start gRPC server...");
            server.start();
            System.out.println("Auth gRPC Server started, listening on " + server.getPort());
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
