package com.piggy.microservice.statistics.grpc.client;

import com.piggy.microservice.statistics.domain.*;
import com.piggy.microservice.statistics.domain.timeseries.DataPoint;
import com.piggy.microservice.statistics.domain.timeseries.DataPointId;
import com.piggy.microservice.statistics.domain.timeseries.ItemMetric;
import com.piggy.microservice.statistics.domain.timeseries.StatisticMetric;
import com.piggy.microservice.statistics.grpc.StatisticsProto;
import com.piggy.microservice.statistics.grpc.StatisticsServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class statisticsClientImpl implements StatisticsClient {

    private static final Logger logger = Logger.getLogger(statisticsClientImpl.class.getName());
    private final StatisticsServiceGrpc.StatisticsServiceBlockingStub statisticsService;
    private final ManagedChannel channel;

    @Autowired
    public statisticsClientImpl(@Value("${statistics.service.host:localhost}") String host,
                                 @Value("${statistics.server.port:9093}") int port){
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
    public List<DataPoint> getCurrentAccountStatistics(String accountName) {
        // Create the gRPC request
        StatisticsProto.AccountRequest request = StatisticsProto.AccountRequest.newBuilder()
                .setName(accountName)
                .build();

        // Call the gRPC service and get the response
        StatisticsProto.AccountStatisticsResponse response = statisticsService.getCurrentAccountStatistics(request);

        // Convert the gRPC response to domain objects
        return response.getDataPointsList().stream().map(protoDataPoint -> {
            // Convert DataPointId
            DataPointId id = new DataPointId(
                    protoDataPoint.getId().getAccount(),
                    java.sql.Date.valueOf(protoDataPoint.getId().getDate())
            );

            // Convert incomes
            Set<ItemMetric> incomes = protoDataPoint.getIncomesList().stream().map(protoItem -> new ItemMetric(protoItem.getTitle(), new BigDecimal(protoItem.getAmount()))).collect(Collectors.toSet());

            // Convert expenses
            Set<ItemMetric> expenses = protoDataPoint.getExpensesList().stream().map(protoItem -> new ItemMetric(protoItem.getTitle(), new BigDecimal(protoItem.getAmount()))).collect(Collectors.toSet());

            // Convert statistics
            Map<StatisticMetric, BigDecimal> statistics = protoDataPoint.getStatisticsList().stream().collect(Collectors.toMap(
                    protoEntry -> StatisticMetric.valueOf(protoEntry.getMetric().name()), // Convert Protobuf enum to Java enum
                    protoEntry -> new BigDecimal(protoEntry.getValue())
            ));

            // Convert rates
            Map<Currency, BigDecimal> rates = protoDataPoint.getRatesList().stream().collect(Collectors.toMap(
                    protoEntry -> Currency.valueOf(protoEntry.getCurrency().name()), // Convert Protobuf enum to Java enum
                    protoEntry -> new BigDecimal(protoEntry.getRate())
            ));
            DataPoint dataPoint = new DataPoint();
            dataPoint.setId(id);
            dataPoint.setIncomes(incomes);
            dataPoint.setExpenses(expenses);
            dataPoint.setStatistics(statistics);
            dataPoint.setRates(rates);
            // Create and return DataPoint
            return dataPoint;
        }).collect(Collectors.toList());
    }


    @Override
    public String updateAccountStatistics(String accountName, Account account) {
        // Convert domain Account to Protobuf Account
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

//        StatisticsProto.Account protoAccount = StatisticsProto.Account.newBuilder()
//                .addAllIncomes(account.getIncomes().stream().map(item ->
//                        StatisticsProto.Item.newBuilder()
//                                .setTitle(item.getTitle())
//                                .setAmount(item.getAmount().toString())
//                                .setCurrency(StatisticsProto.Currency.valueOf(item.getCurrency().name())) // Convert Java enum to Protobuf enum
//                                .setPeriod(convertTimePeriodToProto(item.getPeriod())) // Convert Java TimePeriod to Protobuf enum
//                                .build()
//                ).collect(Collectors.toList()))
//                .addAllExpenses(account.getExpenses().stream().map(item ->
//                        StatisticsProto.Item.newBuilder()
//                                .setTitle(item.getTitle())
//                                .setAmount(item.getAmount().toString())
//                                .setCurrency(StatisticsProto.Currency.valueOf(item.getCurrency().name())) // Convert Java enum to Protobuf enum
//                                .setPeriod(convertTimePeriodToProto(item.getPeriod())) // Convert Java TimePeriod to Protobuf enum
//                                .build()
//                ).collect(Collectors.toList()))
//                .setSaving(StatisticsProto.Saving.newBuilder()
//                        .setAmount(account.getSaving().getAmount().toString())
//                        .setCurrency(StatisticsProto.Currency.valueOf(account.getSaving().getCurrency().name())) // Convert Java enum to Protobuf enum
//                        .setInterest(account.getSaving().getInterest().toString())
//                        .setDeposit(account.getSaving().getDeposit())
//                        .setCapitalization(account.getSaving().getCapitalization())
//                        .build()
//                )
//                .build();
//
//        // Create the gRPC request
//        StatisticsProto.UpdateAccountRequest request = StatisticsProto.UpdateAccountRequest.newBuilder()
//                .setName(accountName)
//                .setAccount(protoAccount)
//                .build();
//
//        // Call the gRPC service and get the response
//        StatisticsProto.UpdateAccountResponse response = statisticsService.updateAccountStatistics(request);
//
//        // Return the response message
//        return response.getMessage();
    }

    // Utility method to convert Java TimePeriod to Protobuf TimePeriod
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


    private StatisticsProto.Currency convertToGrpcCurrency(Currency currency) {
        switch (currency) {
            case USD:
                return StatisticsProto.Currency.USD;
            case EUR:
                return StatisticsProto.Currency.EUR;
            case RUB:
                return StatisticsProto.Currency.RUB;
            default:
                throw new IllegalArgumentException("Unknown currency: " + currency);
        }
    }

}
