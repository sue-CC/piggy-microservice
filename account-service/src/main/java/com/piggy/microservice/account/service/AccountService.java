package com.piggy.microservice.account.service;

import com.piggy.microservice.account.domain.Account;
import com.piggy.microservice.account.domain.User;

public interface AccountService {


    Account findByName(String accountName);

    /**
     * Checks if account with the same name already exists
     * Invokes Auth Service user creation
     * Creates new account with default parameters
     *
     * @param user
     * @return created account
     */
    Account create(User user);

    /**
     * Validates and applies incoming account updates
     * Invokes Statistics Service update
     *
     * @param name
     * @param update
     */

    void saveChanges(String name, Account update);
}
