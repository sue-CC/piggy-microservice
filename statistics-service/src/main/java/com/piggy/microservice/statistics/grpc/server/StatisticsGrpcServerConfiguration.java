package com.piggy.microservice.statistics.grpc.server;

import com.piggy.microservice.statistics.service.StatisticsServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class StatisticsGrpcServerConfiguration {
    private final Server server;
    private final StatisticsServiceImpl statisticsService;


    public StatisticsGrpcServerConfiguration(@Value("${grpc.server.port:9093}")int port, StatisticsServiceImpl statisticsService) {
        this.statisticsService = statisticsService;
        System.out.println("StatisticsService injected: " + (statisticsService != null));
        ServerBuilder<?> builder = ServerBuilder.forPort(port);
        builder.addService(new StatisticsGrpcServiceImpl(statisticsService));
        this.server = builder.build();
    }

    @PostConstruct
    public void start() {
        try {
            System.out.println("Attempting to start gRPC server...");
            server.start();
            System.out.println("Statistics gRPC Server started, listening on " + server.getPort());
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
