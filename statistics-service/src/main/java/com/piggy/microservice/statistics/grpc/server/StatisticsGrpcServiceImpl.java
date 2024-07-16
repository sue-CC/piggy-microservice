package com.piggy.microservice.statistics.grpc.server;

import com.piggy.microservice.statistics.domain.Account;
import com.piggy.microservice.statistics.domain.Currency;
import com.piggy.microservice.statistics.domain.Saving;
import com.piggy.microservice.statistics.domain.TimePeriod;
import com.piggy.microservice.statistics.domain.timeseries.DataPoint;
import com.piggy.microservice.statistics.domain.Item;
import com.piggy.microservice.statistics.domain.timeseries.ItemMetric;
import com.piggy.microservice.statistics.domain.timeseries.StatisticMetric;
import com.piggy.microservice.statistics.grpc.StatisticsProto;
import com.piggy.microservice.statistics.grpc.StatisticsServiceGrpc;
import com.piggy.microservice.statistics.service.StatisticsServiceImpl;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

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

        StatisticsProto.AccountStatisticsResponse.Builder responseBuilder = StatisticsProto.AccountStatisticsResponse.newBuilder();
        for (DataPoint dataPoint : dataPoints) {
            StatisticsProto.DataPoint.Builder grpcDataPoint = StatisticsProto.DataPoint.newBuilder()
                    .setId(StatisticsProto.DataPointId.newBuilder()
                            .setAccount(dataPoint.getId().getAccount())
                            .setDate(new SimpleDateFormat("yyyy-MM-dd").format(dataPoint.getId().getDate()))
                            .build());

            for (ItemMetric income : dataPoint.getIncomes()) {
                grpcDataPoint.addIncomes(StatisticsProto.Item.newBuilder()
                        .setTitle(income.getTitle())
                        .setAmount(income.getAmount().toString())
                        .build());
            }

            for (ItemMetric expense : dataPoint.getExpenses()) {
                grpcDataPoint.addExpenses(StatisticsProto.Item.newBuilder()
                        .setTitle(expense.getTitle())
                        .setAmount(expense.getAmount().toString())
                        .build());
            }

            for (Map.Entry<StatisticMetric, BigDecimal> entry : dataPoint.getStatistics().entrySet()) {
                grpcDataPoint.addStatistics(
                        StatisticsProto.StatisticEntry.newBuilder()
                                .setMetric(StatisticsProto.StatisticMetric.valueOf(entry.getKey().name()))
                                .setValue(entry.getValue().toString())
                                .build()
                );
            }

            for (Map.Entry<Currency, BigDecimal> entry : dataPoint.getRates().entrySet()) {
                grpcDataPoint.addRates(
                        StatisticsProto.CurrencyEntry.newBuilder()
                                .setCurrency(StatisticsProto.Currency.valueOf(entry.getKey().name())) // Convert Java enum to Protobuf enum
                                .setRate(entry.getValue().toString())
                                .build()
                );
            }

            responseBuilder.addDataPoints(grpcDataPoint.build());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateAccountStatistics(StatisticsProto.UpdateAccountRequest request, StreamObserver<StatisticsProto.UpdateAccountResponse> responseObserver) {
        Account account = new Account();
        account.setExpenses(convertItemsFromProto(request.getAccount().getExpensesList()));

//        System.out.println("IncomeList:" + request.getAccount().getIncomesList());

        account.setIncomes(convertItemsFromProto(request.getAccount().getIncomesList()));
        account.setSaving(convertSavingFromProto(request.getAccount().getSaving()));

        statisticsService.save(request.getName(), account);

        StatisticsProto.UpdateAccountResponse response = StatisticsProto.UpdateAccountResponse.newBuilder()
                .setMessage("Account: " + request.getName() + " has been updated.")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
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
//            System.out.println("title:" + item.getTitle());
            String amountStr = protoItem.getAmount();
            item.setAmount(new BigDecimal(amountStr));
//            System.out.println("amount:" + item.getAmount());
            // currency
            item.setCurrency(Currency.valueOf(protoItem.getCurrency().name()));

            // time period
            item.setPeriod(convertTimePeriodFromProto(protoItem.getPeriod()));

            return item;
        }).toList();
    }

    private Saving convertSavingFromProto(StatisticsProto.Saving protoSaving) {
        Saving saving = new Saving();

        // Validate and convert amount
        String amountStr = protoSaving.getAmount();
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new NumberFormatException("Input amount string is null or empty");
        }
        try {
            saving.setAmount(new BigDecimal(amountStr));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid BigDecimal input: " + amountStr);
        }

        saving.setCurrency(Currency.valueOf(protoSaving.getCurrency().name())); // Convert Protobuf enum to Java enum

        // Validate and convert interest
        String interestStr = protoSaving.getInterest();
        if (interestStr == null || interestStr.trim().isEmpty()) {
            throw new NumberFormatException("Input interest string is null or empty");
        }
        try {
            saving.setInterest(new BigDecimal(interestStr));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid BigDecimal input: " + interestStr);
        }

        saving.setDeposit(protoSaving.getDeposit()); // Convert boolean
        saving.setCapitalization(protoSaving.getCapitalization()); // Convert boolean
        return saving;
    }


}
