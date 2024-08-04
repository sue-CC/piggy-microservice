package com.piggy.microservice.account.grpc.server;

import com.piggy.microservice.account.clients.AuthClientImpl;
import com.piggy.microservice.account.clients.StatisticsClientImpl;
import com.piggy.microservice.account.domain.*;
import com.piggy.microservice.account.grpc.AccountProto;
import com.piggy.microservice.account.grpc.AccountServiceGrpc;
import com.piggy.microservice.account.grpc.StatisticsProto;
import com.piggy.microservice.account.repository.AccountRepository;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.piggy.microservice.account.grpc.client.accountGrpcClientImpl.getTimePeriod;

@Service
public class AccountGrpcServiceImpl extends AccountServiceGrpc.AccountServiceImplBase {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AccountRepository accountRepository;
    private final AuthClientImpl authService;
    private final StatisticsClientImpl statisticsService;

    @Autowired
    public AccountGrpcServiceImpl(AccountRepository accountRepository, AuthClientImpl authService, StatisticsClientImpl statisticsService) {
        this.accountRepository = accountRepository;
        this.authService = authService;
        this.statisticsService = statisticsService;
    }

    @Override
    public void getAccountByName(AccountProto.GetAccountRequest request, StreamObserver<AccountProto.GetAccountResponse> responseObserver) {
        Account account = accountRepository.findByName(request.getName());
        if (account != null) {
            AccountProto.GetAccountResponse response = AccountProto.GetAccountResponse.newBuilder()
                    .setAccount(convertToGrpcAccount(account))
                    .build();
            responseObserver.onNext(response);
        } else {
            responseObserver.onError(new Exception("Account not found"));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void saveCurrentAccount(AccountProto.SaveAccountRequest request, StreamObserver<AccountProto.SuccessMessage> responseObserver) {
        String accountName = request.getAccountName();
        Account account = accountRepository.findByName(accountName);
        if (account == null) {
            User user = new User();
            user.setUsername(accountName);
            user.setPassword("1234");
            authService.createUser(user);
            account = new Account();
            account.setName(accountName);
        }
        account.setIncomes(request.getIncomesList().stream()
                .map(this::convertFromGrpcItem)
                .collect(Collectors.toList()));
        account.setExpenses(request.getExpensesList().stream()
                .map(this::convertFromGrpcItem)
                .collect(Collectors.toList()));
        account.setSaving(convertFromGrpcSaving(request.getSaving()));

        accountRepository.save(account);
        statisticsService.updateAccountStatistics(accountName, account);

        AccountProto.SuccessMessage response = AccountProto.SuccessMessage.newBuilder()
                .setSuccessMessage("Account updated successfully.")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        log.info("save changes have been saved: " + account.getName());
    }

    @Override
    public void createNewAccount(AccountProto.CreateAccountRequest request, StreamObserver<AccountProto.GetAccountResponse> responseObserver) {
        String username = request.getUsername();
        User user = new User();
        user.setUsername(username);
        user.setPassword(request.getPassword());
        authService.createUser(user);

        Saving saving = new Saving();
        saving.setAmount(BigDecimal.ZERO);
        saving.setCurrency(Currency.getDefault());
        saving.setInterest(BigDecimal.ZERO);
        saving.setDeposit(false);
        saving.setCapitalization(false);

        Account account = new Account();
        account.setName(username);
        account.setSaving(saving);

        accountRepository.save(account);
        AccountProto.GetAccountResponse response = AccountProto.GetAccountResponse.newBuilder()
                .setAccount(convertToGrpcAccount(account))
                .build();
        responseObserver.onNext(response);
        log.info("new account has been created: " + account.getName());
        responseObserver.onCompleted();
    }

    private AccountProto.Account convertToGrpcAccount(Account account) {
        return AccountProto.Account.newBuilder()
                .setName(account.getName())
                .addAllIncomes(account.getIncomes() != null ?
                        account.getIncomes().stream().map(this::convertToGrpcItem).collect(Collectors.toList()) :
                        Collections.emptyList())
                .addAllExpenses(account.getExpenses() != null ?
                        account.getExpenses().stream().map(this::convertToGrpcItem).collect(Collectors.toList()) :
                        Collections.emptyList())
                .setSaving(convertToGrpcSaving(account.getSaving()))
                .setNote(account.getNote() != null ? account.getNote() : "")
                .build();
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

    private StatisticsProto.Currency convertToGrpcCurrency(Currency currency) {
        return switch (currency) {
            case USD -> StatisticsProto.Currency.USD;
            case EUR -> StatisticsProto.Currency.EUR;
            case RUB -> StatisticsProto.Currency.RUB;
        };
    }

    private Item convertFromGrpcItem(StatisticsProto.Item grpcItem) {
        Item item = new Item();
        item.setTitle(grpcItem.getTitle());
        item.setAmount(new BigDecimal(grpcItem.getAmount()));
        item.setCurrency(convertFromGrpcCurrency(grpcItem.getCurrency()));
        item.setPeriod(convertFromGrpcPeriod(grpcItem.getPeriod()));
        return item;
    }

    private Saving convertFromGrpcSaving(StatisticsProto.Saving grpcSaving) {
        Saving saving = new Saving();
        saving.setAmount(new BigDecimal(grpcSaving.getAmount()));
        saving.setCurrency(convertFromGrpcCurrency(grpcSaving.getCurrency()));
        saving.setInterest(new BigDecimal(grpcSaving.getInterest()));
        saving.setDeposit(grpcSaving.getDeposit());
        saving.setCapitalization(grpcSaving.getCapitalization());
        return saving;
    }

    private Currency convertFromGrpcCurrency(StatisticsProto.Currency grpcCurrency) {
        return switch (grpcCurrency) {
            case USD -> Currency.USD;
            case EUR -> Currency.EUR;
            case RUB -> Currency.RUB;
            default -> throw new IllegalArgumentException("Unknown currency: " + grpcCurrency);
        };
    }

    private TimePeriod convertFromGrpcPeriod(StatisticsProto.TimePeriod grpcPeriod) {
        return getTimePeriod(grpcPeriod);
    }
}
