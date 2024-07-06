package com.piggy.microservice.notification.domain;

import java.util.stream.Stream;

public enum Frequency {

    LOW(60), MEDIUM(30), HIGH(10);
    private int seconds;
    Frequency(int seconds) {
        this.seconds = seconds;
    }

    public int getSeconds() {
        return seconds;
    }

    public static Frequency withSeconds(int seconds) {
        return Stream.of(Frequency.values())
                .filter(f -> f.getSeconds() == seconds)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
