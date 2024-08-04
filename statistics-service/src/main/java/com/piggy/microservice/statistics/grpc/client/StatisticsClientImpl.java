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
public class StatisticsClientImpl implements StatisticsClient {

    private static final Logger logger = Logger.getLogger(StatisticsClientImpl.class.getName());
    private final StatisticsServiceGrpc.StatisticsServiceBlockingStub statisticsService;
    private final ManagedChannel channel;

    @Autowired
    public StatisticsClientImpl(@Value("${statistics.server.host:statistics-service-grpc}") String host,
                                @Value("${statistics.server.port:9093}") int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
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
        logger.info("Requesting current account statistics for account: " + accountName);

        StatisticsProto.AccountRequest request = StatisticsProto.AccountRequest.newBuilder()
                .setName(accountName)
                .build();

        StatisticsProto.AccountStatisticsResponse response;
        try {
            response = statisticsService.getCurrentAccountStatistics(request);
        } catch (Exception e) {
            logger.severe("Failed to get current account statistics: " + e.getMessage());
            throw new RuntimeException("Failed to get current account statistics", e);
        }

        return response.getDataPointsList().stream()
                .map(this::convertProtoDataPointToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public String updateAccountStatistics(String accountName, Account account) {
        logger.info("Updating account statistics for account: " + accountName);

        StatisticsProto.UpdateAccountRequest request = StatisticsProto.UpdateAccountRequest.newBuilder()
                .setName(accountName)
                .setUpdate(convertAccountToProtoUpdateAccount(account))
                .build();

        StatisticsProto.UpdateAccountResponse response;
        try {
            response = statisticsService.updateAccountStatistics(request);
        } catch (Exception e) {
            logger.severe("Failed to update account statistics: " + e.getMessage());
            throw new RuntimeException("Failed to update account statistics", e);
        }

        return response.getMessage();
    }

    private StatisticsProto.UpdateAccount convertAccountToProtoUpdateAccount(Account account) {
        return StatisticsProto.UpdateAccount.newBuilder()
                .addAllIncomes(account.getIncomes().stream()
                        .map(this::convertItemToProto)
                        .collect(Collectors.toList()))
                .addAllExpenses(account.getExpenses().stream()
                        .map(this::convertItemToProto)
                        .collect(Collectors.toList()))
                .setSaving(convertSavingToProto(account.getSaving()))
                .build();
    }

    private StatisticsProto.Item convertItemToProto(Item item) {
        return StatisticsProto.Item.newBuilder()
                .setTitle(item.getTitle())
                .setAmount(item.getAmount().toString())
                .setCurrency(StatisticsProto.Currency.valueOf(item.getCurrency().name()))
                .setPeriod(convertTimePeriodToProto(item.getPeriod()))
                .build();
    }

    private StatisticsProto.Saving convertSavingToProto(Saving saving) {
        return StatisticsProto.Saving.newBuilder()
                .setAmount(saving.getAmount().toString())
                .setCurrency(StatisticsProto.Currency.valueOf(saving.getCurrency().name()))
                .setInterest(saving.getInterest().toString())
                .setDeposit(saving.getDeposit())
                .setCapitalization(saving.getCapitalization())
                .build();
    }

    private StatisticsProto.TimePeriod convertTimePeriodToProto(TimePeriod timePeriod) {
        return switch (timePeriod) {
            case YEAR -> StatisticsProto.TimePeriod.YEAR;
            case QUARTER -> StatisticsProto.TimePeriod.QUARTER;
            case MONTH -> StatisticsProto.TimePeriod.MONTH;
            case DAY -> StatisticsProto.TimePeriod.DAY;
            case HOUR -> StatisticsProto.TimePeriod.HOUR;
            default -> throw new IllegalArgumentException("Unknown TimePeriod: " + timePeriod);
        };
    }

    private DataPoint convertProtoDataPointToDomain(StatisticsProto.DataPoint protoDataPoint) {
        DataPointId id = new DataPointId(
                protoDataPoint.getId().getAccount(),
                java.sql.Date.valueOf(protoDataPoint.getId().getDate())
        );

        Set<ItemMetric> incomes = protoDataPoint.getIncomesList().stream()
                .map(protoItem -> new ItemMetric(protoItem.getTitle(), new BigDecimal(protoItem.getAmount())))
                .collect(Collectors.toSet());

        Set<ItemMetric> expenses = protoDataPoint.getExpensesList().stream()
                .map(protoItem -> new ItemMetric(protoItem.getTitle(), new BigDecimal(protoItem.getAmount())))
                .collect(Collectors.toSet());

        DataPoint dataPoint = new DataPoint();
        dataPoint.setId(id);
        dataPoint.setIncomes(incomes);
        dataPoint.setExpenses(expenses);

        return dataPoint;
    }
}
