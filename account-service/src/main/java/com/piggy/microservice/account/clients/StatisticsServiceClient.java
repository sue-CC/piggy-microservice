package com.piggy.microservice.account.clients;

import com.piggy.microservice.account.domain.Account;

public interface StatisticsServiceClient {
    String updateAccountStatistics(String accountName, Account account);

}
