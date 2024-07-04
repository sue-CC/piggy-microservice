package com.piggy.microservice.statistics.service;

import com.piggy.microservice.statistics.domain.Currency;
import com.piggy.microservice.statistics.domain.ExchangeRatesContainer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class RateService {

    public ExchangeRatesContainer getRates(Currency base) {
        switch (base) {
            case EUR:
                return createContainer(base, getEurRates());
            case RUB:
                return createContainer(base, getRubRates());
            case USD:
                return createContainer(base, getUsdRates());
            default:
                throw new IllegalArgumentException("Unsupported currency: " + base);
        }
    }

    private ExchangeRatesContainer createContainer(Currency base, Map<String, BigDecimal> rates) {
        ExchangeRatesContainer container = new ExchangeRatesContainer();
        container.setBase(base);
        container.setRates(rates);
        container.setDate(LocalDate.now());
        return container;
    }

    private Map<String, BigDecimal> getEurRates() {
        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("USD", new BigDecimal("1.18"));
        rates.put("RUB", new BigDecimal("87.53"));
        return rates;
    }

    private Map<String, BigDecimal> getRubRates() {
        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("USD", new BigDecimal("0.013"));
        rates.put("EUR", new BigDecimal("0.011"));
        return rates;
    }

    private Map<String, BigDecimal> getUsdRates() {
        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("EUR", new BigDecimal("0.85"));
        rates.put("RUB", new BigDecimal("73.68"));
        return rates;
    }
}