package com.piggy.microservice.account.clients;


public interface StatisticsServiceClient {
    String updateAccountStatistics(String accountName, com.piggy.microservice.statistics.domain.Account account);

}
