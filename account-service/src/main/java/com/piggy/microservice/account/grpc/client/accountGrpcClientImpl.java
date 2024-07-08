package com.piggy.microservice.account.grpc.client;

import com.piggy.microservice.account.domain.*;
import com.piggy.microservice.account.grpc.AccountProto;
import com.piggy.microservice.account.grpc.AccountServiceGrpc;
import io.grpc.*;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class accountGrpcClientImpl implements AccountClient {

    private static final Logger logger = Logger.getLogger(accountGrpcClientImpl.class.getName());
    private final AccountServiceGrpc.AccountServiceBlockingStub accountService;
    private final ManagedChannel channel;

    @Autowired
    public accountGrpcClientImpl(@Value("${order.service.host:localhost}") String host,
                                 @Value("${order.service.port:9090}") int port){
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext().build();
        this.accountService = AccountServiceGrpc.newBlockingStub(channel);
//        .withCallCredentials(callCredentials);
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

    public Account saveCurrentAccount(String accountName, Account account) {
        AccountProto.SaveAccountRequest request = AccountProto.SaveAccountRequest.newBuilder()
                .setAccountName(accountName)
                .addAllIncomes(account.getIncomes().stream()
                        .map(this::convertToGrpcItem)
                        .collect(Collectors.toList()))
                .addAllExpenses(account.getExpenses().stream()
                        .map(this::convertToGrpcItem)
                        .collect(Collectors.toList()))
                .setSaving(convertToGrpcSaving(account.getSaving()))
                .build();

        AccountProto.GetAccountResponse response;
        try {
            response = accountService.saveCurrentAccount(request);
        } catch (StatusRuntimeException e) {
            // Handle gRPC exception
            e.printStackTrace();
            return null;
        }
        return mapToAccount(response);
    }
    @Override
    public Account createNewAccount(User user) {
        AccountProto.CreateAccountRequest request = AccountProto.CreateAccountRequest.newBuilder()
                .setPassword(user.getPassword())
                .setUsername(user.getUsername())
                .build();
        try {
            AccountProto.GetAccountResponse response = accountService.createNewAccount(request);
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
        account.setLastSeen(convertToDate(grpcAccount.getLastSeen()));
        account.setIncomes(grpcAccount.getIncomesList().stream().map(this::mapToItem).collect(Collectors.toList()));
        account.setExpenses(grpcAccount.getExpensesList().stream().map(this::mapToItem).collect(Collectors.toList()));
        account.setSaving(mapToSaving(grpcAccount.getSaving()));
        account.setNote(grpcAccount.getNote());

        return account;
    }

    private Date convertToDate(String dateTime){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            return formatter.parse(dateTime);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private Item mapToItem(AccountProto.Item grpcItem) {
        Item item = new Item();
        item.setTitle(grpcItem.getTitle());
        item.setAmount(new BigDecimal(grpcItem.getAmount()));
        item.setCurrency(mapToCurrency(grpcItem.getCurrency()));
        item.setPeriod(mapToPeriod(grpcItem.getPeriod()));

        return item;
    }

    private Saving mapToSaving(AccountProto.Saving grpcSaving) {
        Saving saving = new Saving();
        saving.setAmount(new BigDecimal(grpcSaving.getAmount()));
        saving.setCurrency(mapToCurrency(grpcSaving.getCurrency()));
        saving.setInterest(new BigDecimal(grpcSaving.getInterest()));
        saving.setDeposit(grpcSaving.getDeposit());
        saving.setCapitalization(grpcSaving.getCapitalization());

        return saving;
    }

    private Currency mapToCurrency(AccountProto.Currency grpcCurrency) {
        switch (grpcCurrency) {
            case USD:
                return Currency.USD;
            case EUR:
                return Currency.EUR;
            case RUB:
                return Currency.RUB;
            default:
                throw new IllegalArgumentException("Unknown currency: " + grpcCurrency);
        }
    }

    private TimePeriod mapToPeriod(AccountProto.TimePeriod grpcPeriod) {
        switch (grpcPeriod) {
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
                throw new IllegalArgumentException("Unknown period: " + grpcPeriod);
        }
    }


    private AccountProto.Account convertToGrpcAccount(Account account) {
        return AccountProto.Account.newBuilder()
                .setName(account.getName())
                .setLastSeen(account.getLastSeen().toString())
                .addAllIncomes(account.getIncomes().stream().map(this::convertToGrpcItem).collect(Collectors.toList()))
                .addAllExpenses(account.getExpenses().stream().map(this::convertToGrpcItem).collect(Collectors.toList()))
                .setSaving(convertToGrpcSaving(account.getSaving()))
                .setNote(account.getNote())
                .build();
    }

    private AccountProto.Item convertToGrpcItem(Item item) {
        return AccountProto.Item.newBuilder()
                .setTitle(item.getTitle())
                .setAmount(item.getAmount().toPlainString())
                .setCurrency(convertToGrpcCurrency(item.getCurrency()))
                .setPeriod(convertToGrpcPeriod(item.getPeriod()))
                .build();
    }

    private AccountProto.Saving convertToGrpcSaving(Saving saving) {
        return AccountProto.Saving.newBuilder()
                .setAmount(saving.getAmount().toPlainString())
                .setCurrency(convertToGrpcCurrency(saving.getCurrency()))
                .setInterest(saving.getInterest().toPlainString())
                .setDeposit(saving.getDeposit())
                .setCapitalization(saving.getCapitalization())
                .build();
    }

    private AccountProto.TimePeriod convertToGrpcPeriod(TimePeriod period) {
        switch (period) {
            case YEAR:
                return AccountProto.TimePeriod.YEAR;
            case QUARTER:
                return AccountProto.TimePeriod.QUARTER;
            case MONTH:
                return AccountProto.TimePeriod.MONTH;
            case DAY:
                return AccountProto.TimePeriod.DAY;
            case HOUR:
                return AccountProto.TimePeriod.HOUR;
            default:
                throw new IllegalArgumentException("Unknown currency: " + period);
        }
    }

    private AccountProto.Currency convertToGrpcCurrency(Currency currency) {
        switch (currency) {
            case USD:
                return AccountProto.Currency.USD;
            case EUR:
                return AccountProto.Currency.EUR;
            case RUB:
                return AccountProto.Currency.RUB;
            default:
                throw new IllegalArgumentException("Unknown currency: " + currency);
        }
    }
    }
