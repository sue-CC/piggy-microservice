package com.piggy.microservice.account.clients;

import com.piggy.microservice.account.domain.Account;

public interface StatisticsServiceClient {
    void updateAccountStatistics(String accountName, Account account);

}
