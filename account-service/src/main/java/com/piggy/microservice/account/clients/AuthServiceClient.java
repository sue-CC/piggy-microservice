package com.piggy.microservice.account.clients;

import com.piggy.microservice.account.domain.User;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

//@FeignClient(name = "auth-service")
public interface AuthServiceClient {
    String createUser(User user);

}
