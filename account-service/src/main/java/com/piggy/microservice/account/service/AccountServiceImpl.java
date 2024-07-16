package com.piggy.microservice.account.service;

import com.piggy.microservice.account.clients.AuthServiceClient;
import com.piggy.microservice.account.clients.StatisticsServiceClient;
import com.piggy.microservice.account.domain.Account;
import com.piggy.microservice.account.domain.Currency;
import com.piggy.microservice.account.domain.Saving;
import com.piggy.microservice.account.domain.User;
import com.piggy.microservice.account.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService{

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AccountRepository accountRepository;
    private final AuthServiceClient authServiceClient;
    private final StatisticsServiceClient statisticsServiceClient;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, AuthServiceClient authServiceClient, StatisticsServiceClient statisticsServiceClient) {
        this.accountRepository = accountRepository;
        this.authServiceClient = authServiceClient;
        this.statisticsServiceClient = statisticsServiceClient;
    }


    @Override
    public Account findByName(String accountName) {
        log.info("findByName accountName={}", accountRepository.findByName(accountName));
       return accountRepository.findByName(accountName);
    }

    @Override
    public Account create(User user) {

        Account existing = accountRepository.findByName(user.getUsername());
        Assert.isNull(existing, "account already exists: " + user.getUsername());

        authServiceClient.createUser(user);

        Saving saving = new Saving();
        saving.setAmount(new BigDecimal(0));
        saving.setCurrency(Currency.getDefault());
        saving.setInterest(new BigDecimal(0));
        saving.setDeposit(false);
        saving.setCapitalization(false);

        Account account = new Account();
        account.setName(user.getUsername());
        account.setSaving(saving);

        accountRepository.save(account);

        log.info("new account has been created: " + account.getName());

        return account;
    }

    @Override
    public void saveChanges(String name, Account update) {
        Account account = accountRepository.findByName(name);
        Assert.notNull(account, "can't find account with name " + name);

        account.setName(name);
        account.setIncomes(update.getIncomes());
        account.setExpenses(update.getExpenses());
        account.setSaving(update.getSaving());
        account.setNote(update.getNote());
        accountRepository.save(account);
        log.info("save changes has been saved: " + account.getName());

        statisticsServiceClient.updateStatistics(name, update);
    }
}
