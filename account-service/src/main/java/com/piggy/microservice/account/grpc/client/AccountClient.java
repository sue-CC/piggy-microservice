package com.piggy.microservice.account.grpc.client;

import com.piggy.microservice.account.domain.Account;
import com.piggy.microservice.account.domain.User;
import com.piggy.microservice.account.domain.Item;
import com.piggy.microservice.account.domain.Saving;

import java.util.List;


public interface AccountClient {
   Account getAccountByName(String accountName);
   String saveCurrentAccount(String accountName,
                             Account account);
   Account createNewAccount(User user);
}
