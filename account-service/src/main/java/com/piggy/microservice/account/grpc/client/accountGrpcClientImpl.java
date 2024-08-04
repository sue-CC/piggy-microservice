package com.piggy.microservice.account.grpc.client;

import com.piggy.microservice.account.domain.*;
import com.piggy.microservice.account.grpc.AccountProto;
import com.piggy.microservice.account.grpc.AccountServiceGrpc;
import com.piggy.microservice.account.grpc.StatisticsProto;
import io.grpc.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class accountGrpcClientImpl implements AccountClient {

    private static final Logger logger = Logger.getLogger(accountGrpcClientImpl.class.getName());
    private final AccountServiceGrpc.AccountServiceBlockingStub accountService;
    private final ManagedChannel channel;

    @Autowired
    public accountGrpcClientImpl(@Value("${account.server.host:account-service-grpc}") String host,
                                 @Value("${account.server.port:9090}") int port){
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext().build();
        this.accountService = AccountServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    @Override
    public Account getAccountByName(String accountName) {
        AccountProto.GetAccountRequest request = AccountProto.GetAccountRequest.newBuilder()
                .setName(accountName)
                .build();
        try {
            AccountProto.GetAccountResponse response = accountService.getAccountByName(request);
            return mapToAccount(response);
        } catch (StatusRuntimeException e) {
            // Handle gRPC exception
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String saveCurrentAccount(String accountName,
                                     Account account) {
        AccountProto.SaveAccountRequest request = AccountProto.SaveAccountRequest.newBuilder()
                .setAccountName(accountName)
                .addAllIncomes(account.getIncomes().stream().map(this::convertToGrpcItem).collect(Collectors.toList()))
                .addAllExpenses(account.getExpenses().stream().map(this::convertToGrpcItem).collect(Collectors.toList()))
                .setSaving(convertToGrpcSaving(account.getSaving()))
                .build();
        AccountProto.SuccessMessage response = accountService.saveCurrentAccount(request);
        logger.info(response.getSuccessMessage());
        return response.getSuccessMessage();
    }

    @Override
    public Account createNewAccount(User user) {
        AccountProto.CreateAccountRequest request = AccountProto.CreateAccountRequest.newBuilder()
                .setPassword(user.getPassword())
                .setUsername(user.getUsername())
                .build();
        try {
            AccountProto.GetAccountResponse response = accountService.createNewAccount(request);
            logger.info("new account have been saved: " + response.getAccount().getName());
            return mapToAccount(response);
        } catch (StatusRuntimeException e) {
            // Handle gRPC exception
            e.printStackTrace();
            return null;
        }
    }

    private Account mapToAccount(AccountProto.GetAccountResponse response) {
        AccountProto.Account grpcAccount = response.getAccount();

        Account account = new Account();
        account.setName(grpcAccount.getName());
        account.setIncomes(grpcAccount.getIncomesList().stream().map(this::mapToItem).collect(Collectors.toList()));
        account.setExpenses(grpcAccount.getExpensesList().stream().map(this::mapToItem).collect(Collectors.toList()));
        account.setSaving(mapToSaving(grpcAccount.getSaving()));
        account.setNote(grpcAccount.getNote());

        return account;
    }


    private Item mapToItem(StatisticsProto.Item grpcItem) {
        Item item = new Item();
        item.setTitle(grpcItem.getTitle());
        item.setAmount(new BigDecimal(grpcItem.getAmount()));
        item.setCurrency(mapToCurrency(grpcItem.getCurrency()));
        item.setPeriod(mapToPeriod(grpcItem.getPeriod()));

        return item;
    }

    private Saving mapToSaving(StatisticsProto.Saving grpcSaving) {
        Saving saving = new Saving();
        saving.setAmount(new BigDecimal(grpcSaving.getAmount()));
        saving.setCurrency(mapToCurrency(grpcSaving.getCurrency()));
        saving.setInterest(new BigDecimal(grpcSaving.getInterest()));
        saving.setDeposit(grpcSaving.getDeposit());
        saving.setCapitalization(grpcSaving.getCapitalization());

        return saving;
    }

    private Currency mapToCurrency(StatisticsProto.Currency grpcCurrency) {
        return switch (grpcCurrency) {
            case USD -> Currency.USD;
            case EUR -> Currency.EUR;
            case RUB -> Currency.RUB;
            default -> throw new IllegalArgumentException("Unknown currency: " + grpcCurrency);
        };
    }

    private TimePeriod mapToPeriod(StatisticsProto.TimePeriod grpcPeriod) {
        return getTimePeriod(grpcPeriod);
    }

    @NotNull
    public static TimePeriod getTimePeriod(StatisticsProto.TimePeriod grpcPeriod) {
        return switch (grpcPeriod) {
            case YEAR -> TimePeriod.YEAR;
            case QUARTER -> TimePeriod.QUARTER;
            case MONTH -> TimePeriod.MONTH;
            case DAY -> TimePeriod.DAY;
            case HOUR -> TimePeriod.HOUR;
            default -> throw new IllegalArgumentException("Unknown period: " + grpcPeriod);
        };
    }

    private StatisticsProto.Item convertToGrpcItem(Item item) {
        return StatisticsProto.Item.newBuilder()
                .setTitle(item.getTitle())
                .setAmount(item.getAmount().toPlainString())
                .setCurrency(convertToGrpcCurrency(item.getCurrency()))
                .setPeriod(convertToGrpcPeriod(item.getPeriod()))
                .build();
    }

    private StatisticsProto.Saving convertToGrpcSaving(Saving saving) {
        return StatisticsProto.Saving.newBuilder()
                .setAmount(saving.getAmount().toPlainString())
                .setCurrency(convertToGrpcCurrency(saving.getCurrency()))
                .setInterest(saving.getInterest().toPlainString())
                .setDeposit(saving.getDeposit())
                .setCapitalization(saving.getCapitalization())
                .build();
    }

    private StatisticsProto.TimePeriod convertToGrpcPeriod(TimePeriod period) {
        return getTimePeriod(period);
    }

    @NotNull
    public static StatisticsProto.TimePeriod getTimePeriod(TimePeriod period) {
        return switch (period) {
            case YEAR -> StatisticsProto.TimePeriod.YEAR;
            case QUARTER -> StatisticsProto.TimePeriod.QUARTER;
            case MONTH -> StatisticsProto.TimePeriod.MONTH;
            case DAY -> StatisticsProto.TimePeriod.DAY;
            case HOUR -> StatisticsProto.TimePeriod.HOUR;
        };
    }

    private StatisticsProto.Currency convertToGrpcCurrency(Currency currency) {
        return switch (currency) {
            case USD -> StatisticsProto.Currency.USD;
            case EUR -> StatisticsProto.Currency.EUR;
            case RUB -> StatisticsProto.Currency.RUB;
        };
    }
    }
