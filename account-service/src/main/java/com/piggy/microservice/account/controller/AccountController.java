package com.piggy.microservice.account.controller;

import com.piggy.microservice.account.domain.Account;
import com.piggy.microservice.account.domain.User;
import com.piggy.microservice.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Get an account by its name.
     *
     * @param name the name of the account to retrieve
     * @return the account with the specified name
     */
    @GetMapping("/{name}")
    public ResponseEntity<Account> getAccountByName(@PathVariable("name") String name) {
        Account account = accountService.findByName(name);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(account);
    }

    /**
     * Update an existing account.
     *
     * @param name the name of the account to update
     * @param account the updated account details
     * @return the updated account
     */
    @RequestMapping(value = "/{name}", method =  RequestMethod.PUT)
    public ResponseEntity<Account> updateAccount(@PathVariable("name") String name, @RequestBody Account account) {
        accountService.saveChanges(name, account);
            return ResponseEntity.ok(account);
    }

    @RequestMapping(path = "/", method = RequestMethod.POST)
    public Account createNewAccount(@RequestBody User user) {
        return accountService.create(user);
    }

    @RequestMapping(path = "/ping", method = RequestMethod.GET)
    public String test() {
        return "pong";
    }
}

