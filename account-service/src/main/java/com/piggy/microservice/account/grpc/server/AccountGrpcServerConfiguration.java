package com.piggy.microservice.account.grpc.server;

import com.piggy.microservice.account.repository.AccountRepository;
import com.piggy.microservice.account.service.AccountServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class AccountGrpcServerConfiguration {
    private final Server server;
    private final AccountServiceImpl accountService;


    public AccountGrpcServerConfiguration(@Value("${grpc.server.port:9090}")int port, AccountRepository accountRepository, AccountServiceImpl accountService) {
        this.accountService = accountService;
        System.out.println("AccountService injected: " + (accountService != null));
        ServerBuilder<?> builder = ServerBuilder.forPort(port);
        builder.addService(new AccountGrpcServiceImpl(accountService));
//                .intercept(new AuthInterceptor());
        this.server = builder.build();
    }

    @PostConstruct
    public void start() {
        try {
            System.out.println("Attempting to start gRPC server...");
            server.start();
            System.out.println("Account gRPC Server started, listening on " + server.getPort());
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
