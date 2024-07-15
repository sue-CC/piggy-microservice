package com.piggy.microservice.notification.client;

public interface AccountServiceClient {
    String getAccount(String name);
}
// get account name from the account-service