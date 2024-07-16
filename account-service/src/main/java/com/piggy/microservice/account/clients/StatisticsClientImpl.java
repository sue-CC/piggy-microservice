package com.piggy.microservice.account.clients;

import com.piggy.microservice.account.domain.Account;
import com.piggy.microservice.account.grpc.AccountProto;
import com.piggy.microservice.statistics.grpc.StatisticsProto;
import com.piggy.microservice.statistics.grpc.StatisticsServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class StatisticsClientImpl implements StatisticsServiceClient {
    private static final Logger logger = Logger.getLogger(StatisticsClientImpl.class.getName());
    private final StatisticsServiceGrpc.StatisticsServiceBlockingStub statisticsService;
    private final ManagedChannel channel;

    @Autowired
    public StatisticsClientImpl(@Value("${statistics.server.host:statistics-service-grpc}") String host,
                                @Value("${statistics.server.port:9093}") int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext().build();
        this.statisticsService = StatisticsServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    @Override
    public String updateAccountStatistics(String accountName, Account account) {
        // Convert domain Account to Protobuf Account
        StatisticsProto.AccountS protoAccount = StatisticsProto.AccountS.newBuilder()
                .addAllIncomes(account.getIncomes().stream().map(item ->
                                AccountProto.Item.newBuilder()
                                .setTitle(item.getTitle())
                                .setAmount(item.getAmount().toString())
                                .setCurrency(AccountProto.Currency.valueOf(item.getCurrency().name())) // Convert Java enum to Protobuf enum
                                .setPeriod(AccountProto.TimePeriod.valueOf(item.getPeriod().name()))
                                .build()
                ).collect(Collectors.toList()))
                .addAllExpenses(account.getExpenses().stream().map(item ->
                        AccountProto.Item.newBuilder()
                                .setTitle(item.getTitle())
                                .setAmount(item.getAmount().toString())
                                .setCurrency(AccountProto.Currency.valueOf(item.getCurrency().name())) // Convert Java enum to Protobuf enum
                                .setPeriod(AccountProto.TimePeriod.valueOf(item.getPeriod().name())) // Convert Java TimePeriod to Protobuf enum
                                .build()
                ).collect(Collectors.toList()))
                .setSaving(AccountProto.Saving.newBuilder()
                        .setAmount(account.getSaving().getAmount().toString())
                        .setCurrency(AccountProto.Currency.valueOf(account.getSaving().getCurrency().name())) // Convert Java enum to Protobuf enum
                        .setInterest(account.getSaving().getInterest().toString())
                        .setDeposit(account.getSaving().getDeposit())
                        .setCapitalization(account.getSaving().getCapitalization())
                        .build()
                )
                .build();

        // Create the gRPC request
        StatisticsProto.UpdateAccountRequest request = StatisticsProto.UpdateAccountRequest.newBuilder()
                .setName(accountName)
                .setAccount(protoAccount)
                .build();

        // Call the gRPC service and get the response
        StatisticsProto.UpdateAccountResponse response = statisticsService.updateAccountStatistics(request);

        // Return the response message
        return response.getMessage();
    }



}
