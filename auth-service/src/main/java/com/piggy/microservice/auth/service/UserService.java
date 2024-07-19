package com.piggy.microservice.auth.service;

import com.piggy.microservice.auth.domain.User;
import org.springframework.stereotype.Component;

@Component
public interface UserService {
    String createUser(User user);
    Iterable<String> getAllUsers();
    String updateUser(User user);
}
