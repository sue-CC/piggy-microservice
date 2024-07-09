package com.piggy.microservice.auth.grpc.client;

import java.util.List;

public interface AuthClient {
    String addUser(String username, String password);
    List<String> getUsers();
}
