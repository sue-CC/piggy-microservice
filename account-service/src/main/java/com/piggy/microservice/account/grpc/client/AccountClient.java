package com.piggy.microservice.account.grpc.client;

import com.piggy.microservice.account.domain.Account;
import com.piggy.microservice.account.domain.User;


public interface AccountClient {
   Account getAccountByName(String accountName);
   Account saveCurrentAccount(String accountName, Account account);
   Account createNewAccount(User user);
}
