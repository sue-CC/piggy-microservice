package com.piggy.microservice.account;

import com.piggy.microservice.account.domain.Account;
import com.piggy.microservice.account.repository.AccountRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class AccountApplication {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountApplication(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @PostConstruct
    public void generateTestData() {
        accountRepository.save(new Account());

    }

    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }

}
