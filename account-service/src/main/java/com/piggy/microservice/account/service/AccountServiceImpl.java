package com.piggy.microservice.account.service;

import com.piggy.microservice.account.clients.StatisticsClientImpl;
import com.piggy.microservice.account.clients.authClientImpl;
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
public class AccountServiceImpl implements AccountService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AccountRepository accountRepository;
    private final authClientImpl authClientImpl;
    private final StatisticsClientImpl statisticsServiceClient;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, authClientImpl authClientImpl, StatisticsClientImpl statisticsServiceClient) {
        this.accountRepository = accountRepository;
        this.authClientImpl = authClientImpl;
        this.statisticsServiceClient = statisticsServiceClient;
    }

    @Override
    public Account findByName(String accountName) {
        log.info("findByName accountName={}", accountRepository.findByName(accountName));
        return accountRepository.findByName(accountName);
    }

    @Override
    public Account create(User user) {

        authClientImpl.createUser(user);

        Saving saving = new Saving();
        saving.setAmount(BigDecimal.ZERO);
        saving.setCurrency(Currency.getDefault());
        saving.setInterest(BigDecimal.ZERO);
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
        if (account == null) {
            account = new Account();
            User user = new User();
            user.setUsername(name);
            user.setPassword("1234");
            authClientImpl.createUser(user);
        }

        account.setName(name);
        account.setIncomes(update.getIncomes());
        account.setExpenses(update.getExpenses());
        account.setSaving(update.getSaving());
        account.setNote(update.getNote());
        accountRepository.save(account);

        log.info("save changes have been saved: " + account.getName());


        String responseMessage = statisticsServiceClient.updateAccountStatistics(name, account);
        log.info("Statistics service response: " + responseMessage);
    }


}
