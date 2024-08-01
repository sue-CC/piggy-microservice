package com.piggy.microservice.account.grpc.server;

import com.piggy.microservice.account.clients.StatisticsClientImpl;
import com.piggy.microservice.account.clients.AuthClientImpl;
import com.piggy.microservice.account.repository.AccountRepository;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@EnableDiscoveryClient
@Component
public class AccountGrpcServerConfiguration {
    private final Server server;
    private final AccountRepository accountRepository;
    private final AuthClientImpl authClient;
    private final StatisticsClientImpl statisticsClient;

    public AccountGrpcServerConfiguration(@Value("${account.server.port:9090}")int port, AccountRepository accountRepository, AuthClientImpl authClient, StatisticsClientImpl statisticsClient) {
        this.accountRepository = accountRepository;
        this.authClient = authClient;
        this.statisticsClient = statisticsClient;
        ServerBuilder<?> builder = ServerBuilder.forPort(port);
        builder.addService(new AccountGrpcServiceImpl(accountRepository, authClient, statisticsClient));
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
