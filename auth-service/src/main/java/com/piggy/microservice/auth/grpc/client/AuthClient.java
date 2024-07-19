package com.piggy.microservice.auth.grpc.client;

import com.piggy.microservice.auth.domain.User;

import java.util.List;

public interface AuthClient {
    String addUser(String username, String password);
    List<String> getUsers();
    String updateUser(User user);
}
