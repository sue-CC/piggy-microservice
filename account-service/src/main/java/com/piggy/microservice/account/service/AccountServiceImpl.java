package com.piggy.microservice.account.service;

import com.piggy.microservice.account.clients.authClientImpl;
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
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AccountRepository accountRepository;
    private final authClientImpl authClientImpl;
    private final StatisticsServiceClient statisticsServiceClient;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, authClientImpl authClientImpl, StatisticsServiceClient statisticsServiceClient) {
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
        Account existing = accountRepository.findByName(user.getUsername());
        Assert.isNull(existing, "account already exists: " + user.getUsername());

        authClientImpl.createUser(user);

        Saving saving = new Saving();
        saving.setAmount(BigDecimal.ZERO);
        saving.setCurrency(Currency.getDefault());
        saving.setInterest(BigDecimal.ZERO);
        saving.setDeposit(false);
        saving.setCapitalization(false);

        Account account = new Account();
        account.setName(user.getUsername());
        account.setLastSeen(new Date());
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
        account.setLastSeen(new Date());
        accountRepository.save(account);

        log.info("save changes have been saved: " + account.getName());

        // Convert and update the statistics
        com.piggy.microservice.statistics.domain.Account accountStats = new com.piggy.microservice.statistics.domain.Account();

        // Convert incomes
        accountStats.setIncomes(update.getIncomes().stream()
                .map(this::convertToStatisticsItem)
                .collect(Collectors.toList()));

        // Convert expenses
        accountStats.setExpenses(update.getExpenses().stream()
                .map(this::convertToStatisticsItem)
                .collect(Collectors.toList()));

        // Convert saving
        accountStats.setSaving(convertToStatisticsSaving(update.getSaving()));

        String responseMessage = statisticsServiceClient.updateAccountStatistics(name, accountStats);
        log.info("Statistics service response: " + responseMessage);
    }

    private com.piggy.microservice.statistics.domain.Item convertToStatisticsItem(com.piggy.microservice.account.domain.Item item) {
        com.piggy.microservice.statistics.domain.Item statisticsItem = new com.piggy.microservice.statistics.domain.Item();
        statisticsItem.setTitle(item.getTitle());
        statisticsItem.setAmount(item.getAmount());
        statisticsItem.setCurrency(convertToStatisticsCurrency(item.getCurrency())); // Convert currency
        statisticsItem.setPeriod(convertToStatisticsTimePeriod(item.getPeriod())); // Convert time period
        return statisticsItem;
    }

    private com.piggy.microservice.statistics.domain.Currency convertToStatisticsCurrency(com.piggy.microservice.account.domain.Currency currency) {
        return com.piggy.microservice.statistics.domain.Currency.valueOf(currency.name());
    }

    private com.piggy.microservice.statistics.domain.TimePeriod convertToStatisticsTimePeriod(com.piggy.microservice.account.domain.TimePeriod period) {
        switch (period) {
            case YEAR:
                return com.piggy.microservice.statistics.domain.TimePeriod.YEAR;
            case QUARTER:
                return com.piggy.microservice.statistics.domain.TimePeriod.QUARTER;
            case MONTH:
                return com.piggy.microservice.statistics.domain.TimePeriod.MONTH;
            case DAY:
                return com.piggy.microservice.statistics.domain.TimePeriod.DAY;
            case HOUR:
                return com.piggy.microservice.statistics.domain.TimePeriod.HOUR;
            default:
                throw new IllegalArgumentException("Unknown TimePeriod: " + period);
        }
    }

    private com.piggy.microservice.statistics.domain.Saving convertToStatisticsSaving(com.piggy.microservice.account.domain.Saving saving) {
        if (saving == null) {
            return null;
        }
        com.piggy.microservice.statistics.domain.Saving statisticsSaving = new com.piggy.microservice.statistics.domain.Saving();
        statisticsSaving.setAmount(saving.getAmount());
        statisticsSaving.setCurrency(convertToStatisticsCurrency(saving.getCurrency())); // Convert currency
        statisticsSaving.setInterest(saving.getInterest());
        statisticsSaving.setDeposit(saving.getDeposit());
        statisticsSaving.setCapitalization(saving.getCapitalization());
        return statisticsSaving;
    }
}
