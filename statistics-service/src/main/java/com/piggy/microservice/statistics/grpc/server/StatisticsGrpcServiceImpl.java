package com.piggy.microservice.statistics.grpc.server;

import com.piggy.microservice.statistics.domain.Account;
import com.piggy.microservice.statistics.domain.Currency;
import com.piggy.microservice.statistics.domain.Saving;
import com.piggy.microservice.statistics.domain.TimePeriod;
import com.piggy.microservice.statistics.domain.timeseries.DataPoint;
import com.piggy.microservice.statistics.domain.Item;
import com.piggy.microservice.statistics.grpc.StatisticsProto;
import com.piggy.microservice.statistics.grpc.StatisticsServiceGrpc;
import com.piggy.microservice.statistics.service.StatisticsServiceImpl;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsGrpcServiceImpl extends StatisticsServiceGrpc.StatisticsServiceImplBase {

    private final StatisticsServiceImpl statisticsService;

    @Autowired
    public StatisticsGrpcServiceImpl(StatisticsServiceImpl statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Override
    public void getCurrentAccountStatistics(StatisticsProto.AccountRequest request, StreamObserver<StatisticsProto.AccountStatisticsResponse> responseObserver) {
        List<DataPoint> dataPoints = statisticsService.findByAccountName(request.getName());

        List<StatisticsProto.DataPoint> grpcDataPoints = dataPoints.stream()
                .map(this::buildGrpcDataPoint)
                .collect(Collectors.toList());

        StatisticsProto.AccountStatisticsResponse response = StatisticsProto.AccountStatisticsResponse.newBuilder()
                .addAllDataPoints(grpcDataPoints)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateAccountStatistics(StatisticsProto.UpdateAccountRequest request, StreamObserver<StatisticsProto.UpdateAccountResponse> responseObserver) {
        try {
            Account account = new Account();
            account.setExpenses(convertItemsFromProto(request.getUpdate().getExpensesList()));
            account.setIncomes(convertItemsFromProto(request.getUpdate().getIncomesList()));
            account.setSaving(convertSavingFromProto(request.getUpdate().getSaving()));

            statisticsService.save(request.getName(), account);

            StatisticsProto.UpdateAccountResponse response = StatisticsProto.UpdateAccountResponse.newBuilder()
                    .setMessage("Account: " + request.getName() + " has been updated.")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private StatisticsProto.DataPoint buildGrpcDataPoint(DataPoint dataPoint) {
        StatisticsProto.DataPoint.Builder grpcDataPoint = StatisticsProto.DataPoint.newBuilder()
                .setId(StatisticsProto.DataPointId.newBuilder()
                        .setAccount(dataPoint.getId().getAccount())
                        .setDate(new SimpleDateFormat("yyyy-MM-dd").format(dataPoint.getId().getDate()))
                        .build());

        dataPoint.getIncomes().forEach(income -> grpcDataPoint.addIncomes(
                StatisticsProto.Item.newBuilder()
                        .setTitle(income.getTitle())
                        .setAmount(income.getAmount().toString())
                        .build()
        ));

        dataPoint.getExpenses().forEach(expense -> grpcDataPoint.addExpenses(
                StatisticsProto.Item.newBuilder()
                        .setTitle(expense.getTitle())
                        .setAmount(expense.getAmount().toString())
                        .build()
        ));

        return grpcDataPoint.build();
    }

    private TimePeriod convertTimePeriodFromProto(StatisticsProto.TimePeriod protoTimePeriod) {
        return switch (protoTimePeriod) {
            case YEAR -> TimePeriod.YEAR;
            case QUARTER -> TimePeriod.QUARTER;
            case MONTH -> TimePeriod.MONTH;
            case DAY -> TimePeriod.DAY;
            case HOUR -> TimePeriod.HOUR;
            default -> throw new IllegalArgumentException("Unknown TimePeriod: " + protoTimePeriod);
        };
    }

    private List<Item> convertItemsFromProto(List<StatisticsProto.Item> protoItems) {
        return protoItems.stream().map(protoItem -> {
            Item item = new Item();
            item.setTitle(protoItem.getTitle());
            item.setAmount(convertBigDecimal(protoItem.getAmount()));
            item.setCurrency(Currency.valueOf(protoItem.getCurrency().name()));
            item.setPeriod(convertTimePeriodFromProto(protoItem.getPeriod()));
            return item;
        }).collect(Collectors.toList());
    }

    private Saving convertSavingFromProto(StatisticsProto.Saving protoSaving) {
        Saving saving = new Saving();
        saving.setAmount(convertBigDecimal(protoSaving.getAmount()));
        saving.setCurrency(Currency.valueOf(protoSaving.getCurrency().name()));
        saving.setInterest(convertBigDecimal(protoSaving.getInterest()));
        saving.setDeposit(protoSaving.getDeposit());
        saving.setCapitalization(protoSaving.getCapitalization());
        return saving;
    }

    private BigDecimal convertBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("Input string is null or empty");
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid BigDecimal input: " + value);
        }
    }
}
