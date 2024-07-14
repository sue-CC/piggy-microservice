package com.piggy.microservice.account.clients;

import com.piggy.microservice.account.domain.Account;
import org.springframework.cloud.openfeign.FeignClient;

//@FeignClient(name = "statistics-service")
public interface StatisticsServiceClient {
    String updateAccountStatistics(String accountName, Account account);

}
