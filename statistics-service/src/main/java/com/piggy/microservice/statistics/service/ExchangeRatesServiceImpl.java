package com.piggy.microservice.statistics.service;

import com.google.common.collect.ImmutableMap;
import com.piggy.microservice.statistics.domain.Currency;
import com.piggy.microservice.statistics.domain.ExchangeRatesContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static com.piggy.microservice.statistics.domain.Currency.EUR;

@Service
public class ExchangeRatesServiceImpl implements ExchangeRatesService {
    private static final Logger log = LoggerFactory.getLogger(ExchangeRatesServiceImpl.class);
    private ExchangeRatesContainer container;
    private final RateService rateService;

    @Autowired
    public ExchangeRatesServiceImpl(RateService rateService) {
        this.rateService = rateService;
    }


    @Override
    public Map<Currency, BigDecimal> getCurrentRates() {
        if (container == null || !container.getDate().equals(LocalDate.now())) {
            container = rateService.getRates(Currency.getBase());
            log.info("exchange rates has been updated: {}", container);
        }
        return ImmutableMap.of(
                EUR, container.getRates().get(EUR.name()),
                Currency.RUB, container.getRates().get(Currency.RUB.name()),
                Currency.USD, BigDecimal.ONE
        );
    }

    public BigDecimal convert(Currency from, Currency to, BigDecimal amount) {
        if (from == to) {
            return amount;
        }
        ExchangeRatesContainer ratesContainer = rateService.getRates(from);
        Map<String, BigDecimal> rates = ratesContainer.getRates();
        BigDecimal rate = rates.get(to.name());
        if (rate == null) {
            throw new IllegalArgumentException("Conversion rate not available for " + from + " to " + to);
        }
        return amount.multiply(rate);
    }

}