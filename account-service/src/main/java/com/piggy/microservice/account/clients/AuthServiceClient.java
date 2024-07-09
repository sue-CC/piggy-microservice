package com.piggy.microservice.account.clients;

import com.piggy.microservice.account.domain.User;

public interface AuthServiceClient {
    String createUser(User user);

}
