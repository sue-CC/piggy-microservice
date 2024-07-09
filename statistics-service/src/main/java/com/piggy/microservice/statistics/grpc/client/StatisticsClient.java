package com.piggy.microservice.statistics.grpc.client;

import com.piggy.microservice.statistics.domain.Account;
import com.piggy.microservice.statistics.domain.timeseries.DataPoint;

import java.util.List;

public interface StatisticsClient {
    List<DataPoint> getCurrentAccountStatistics(String accountName);
    String updateAccountStatistics(String accountName, Account account);
}
