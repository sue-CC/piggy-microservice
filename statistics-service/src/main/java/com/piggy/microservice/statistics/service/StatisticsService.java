package com.piggy.microservice.statistics.service;

import com.piggy.microservice.statistics.domain.Account;
import com.piggy.microservice.statistics.domain.timeseries.DataPoint;

import java.util.List;

public interface StatisticsService {
    List<DataPoint> findByAccountName(String accountName);

    DataPoint save(String accountName, Account account);


}
