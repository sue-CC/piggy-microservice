package com.piggy.microservice.account.grpc.server;

import com.piggy.microservice.account.domain.*;
import com.piggy.microservice.account.domain.*;
import com.piggy.microservice.account.grpc.AccountProto;
import com.piggy.microservice.account.grpc.AccountServiceGrpc;
import com.piggy.microservice.account.service.AccountService;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class AccountGrpcServiceImpl extends AccountServiceGrpc.AccountServiceImplBase {

    private final AccountService accountService;

    @Autowired
    public AccountGrpcServiceImpl(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void getAccountByName(AccountProto.GetAccountRequest request, StreamObserver<AccountProto.GetAccountResponse> responseObserver) {
        Account account = accountService.findByName(request.getName());
        AccountProto.GetAccountResponse response = AccountProto.GetAccountResponse.newBuilder()
                .setAccount(convertToGrpcAccount(account))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void saveCurrentAccount(AccountProto.SaveAccountRequest request, StreamObserver<AccountProto.GetAccountResponse> responseObserver) {
        String accountName = request.getAccountName();
        Account account = accountService.findByName(accountName);

        // Update the account with the provided incomes, expenses, and saving
        account.setIncomes(request.getIncomesList().stream()
                .map(this::convertFromGrpcItem)
                .collect(Collectors.toList()));

        account.setExpenses(request.getExpensesList().stream()
                .map(this::convertFromGrpcItem)
                .collect(Collectors.toList()));

        account.setSaving(convertFromGrpcSaving(request.getSaving()));

        // Save the updated account
        accountService.saveChanges(accountName, account);

        // Create the response
        AccountProto.GetAccountResponse response = AccountProto.GetAccountResponse.newBuilder()
                .setAccount(convertToGrpcAccount(account))
                .build();

        // Send the response
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createNewAccount(AccountProto.CreateAccountRequest request, StreamObserver<AccountProto.GetAccountResponse> responseObserver) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        AccountProto.GetAccountResponse response = AccountProto.GetAccountResponse.newBuilder()
                .setAccount(convertToGrpcAccount(accountService.create(user)))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private AccountProto.Account convertToGrpcAccount(Account account) {
        return AccountProto.Account.newBuilder()
                .setName(account.getName())
                .setLastSeen(account.getLastSeen().toString())
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

    private Item convertFromGrpcItem(AccountProto.Item grpcItem) {
        Item item = new Item();
        item.setTitle(grpcItem.getTitle());
        item.setAmount(new BigDecimal(grpcItem.getAmount()));
        item.setCurrency(convertFromGrpcCurrency(grpcItem.getCurrency()));
        item.setPeriod(convertFromGrpcPeriod(grpcItem.getPeriod()));
        item.setIcon(grpcItem.getIcon());
        return item;
    }

    private Saving convertFromGrpcSaving(AccountProto.Saving grpcSaving) {
        Saving saving = new Saving();
        saving.setAmount(new BigDecimal(grpcSaving.getAmount()));
        saving.setCurrency(convertFromGrpcCurrency(grpcSaving.getCurrency()));
        saving.setInterest(new BigDecimal(grpcSaving.getInterest()));
        saving.setDeposit(grpcSaving.getDeposit());
        saving.setCapitalization(grpcSaving.getCapitalization());
        return saving;
    }

    private Currency convertFromGrpcCurrency(AccountProto.Currency grpcCurrency) {
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

    private TimePeriod convertFromGrpcPeriod(AccountProto.TimePeriod grpcPeriod) {
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

}
