package com.piggy.microservice.account.clients;

import com.piggy.microservice.statistics.domain.Account;
import com.piggy.microservice.statistics.domain.TimePeriod;
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
public class statisticsClientImpl implements StatisticsServiceClient{
    private static final Logger logger = Logger.getLogger(statisticsClientImpl.class.getName());
    private final StatisticsServiceGrpc.StatisticsServiceBlockingStub statisticsService;
    private final ManagedChannel channel;

    @Autowired
    public statisticsClientImpl(@Value("${order.service.host:localhost}") String host,
                                @Value("${order.service.port:9093}") int port){
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

    private StatisticsProto.TimePeriod convertTimePeriodToProto(TimePeriod timePeriod) {
        switch (timePeriod) {
            case YEAR:
                return StatisticsProto.TimePeriod.YEAR;
            case QUARTER:
                return StatisticsProto.TimePeriod.QUARTER;
            case MONTH:
                return StatisticsProto.TimePeriod.MONTH;
            case DAY:
                return StatisticsProto.TimePeriod.DAY;
            case HOUR:
                return StatisticsProto.TimePeriod.HOUR;
            default:
                throw new IllegalArgumentException("Unknown TimePeriod: " + timePeriod);
        }
    }

    // Utility method to convert Protobuf TimePeriod to Java TimePeriod
    private TimePeriod convertTimePeriodFromProto(StatisticsProto.TimePeriod protoTimePeriod) {
        switch (protoTimePeriod) {
            case YEAR:
                return TimePeriod.YEAR;
            case QUARTER:
                return TimePeriod.QUARTER;
            case MONTH:
                return TimePeriod.MONTH;
            case DAY:
                return TimePeriod.DAY;
            case HOUR:
                return TimePeriod.HOUR;
            default:
                throw new IllegalArgumentException("Unknown TimePeriod: " + protoTimePeriod);
        }
    }

    @Override
    public String updateAccountStatistics(String accountName, com.piggy.microservice.statistics.domain.Account account) {
        StatisticsProto.Account protoAccount = StatisticsProto.Account.newBuilder()
                .addAllIncomes(account.getIncomes().stream().map(item ->
                        StatisticsProto.Item.newBuilder()
                                .setTitle(item.getTitle())
                                .setAmount(item.getAmount().toString())
                                .setCurrency(StatisticsProto.Currency.valueOf(item.getCurrency().name())) // Convert Java enum to Protobuf enum
                                .setPeriod(convertTimePeriodToProto(item.getPeriod())) // Convert Java TimePeriod to Protobuf enum
                                .build()
                ).collect(Collectors.toList()))
                .addAllExpenses(account.getExpenses().stream().map(item ->
                        StatisticsProto.Item.newBuilder()
                                .setTitle(item.getTitle())
                                .setAmount(item.getAmount().toString())
                                .setCurrency(StatisticsProto.Currency.valueOf(item.getCurrency().name())) // Convert Java enum to Protobuf enum
                                .setPeriod(convertTimePeriodToProto(item.getPeriod())) // Convert Java TimePeriod to Protobuf enum
                                .build()
                ).collect(Collectors.toList()))
                .setSaving(StatisticsProto.Saving.newBuilder()
                        .setAmount(account.getSaving().getAmount().toString())
                        .setCurrency(StatisticsProto.Currency.valueOf(account.getSaving().getCurrency().name())) // Convert Java enum to Protobuf enum
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
