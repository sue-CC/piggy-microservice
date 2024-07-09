package com.piggy.microservice.account.domain;

public enum Currency {
    USD, EUR, RUB;

    public static Currency getDefault() {
        return EUR;
    }
}

