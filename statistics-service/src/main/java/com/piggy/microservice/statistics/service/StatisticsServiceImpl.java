package com.piggy.microservice.statistics.service;

import com.google.common.collect.ImmutableMap;
import com.piggy.microservice.statistics.domain.Account;
import com.piggy.microservice.statistics.domain.Currency;
import com.piggy.microservice.statistics.domain.Item;
import com.piggy.microservice.statistics.domain.Saving;
import com.piggy.microservice.statistics.domain.timeseries.DataPoint;
import com.piggy.microservice.statistics.domain.timeseries.DataPointId;
import com.piggy.microservice.statistics.domain.timeseries.ItemMetric;
import com.piggy.microservice.statistics.domain.timeseries.StatisticMetric;
import com.piggy.microservice.statistics.repository.DataPointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final DataPointRepository dataPointRepository;
    private final ExchangeRatesService exchangeRatesService;

    @Autowired
    public StatisticsServiceImpl(DataPointRepository dataPointRepository, RateService rateService) {
        this.dataPointRepository = dataPointRepository;
        this.exchangeRatesService = new ExchangeRatesServiceImpl(rateService);
    }

    @Override
    public List<DataPoint> findByAccountName(String accountName) {
        Assert.hasLength(accountName, "Account name must not be empty");
        List<DataPoint> dataPoints = dataPointRepository.findByIdAccount(accountName);
        if (dataPoints.isEmpty()) {
            log.info("No data points found for account name: {}", accountName);
        }
        return dataPoints;
    }


    @Override
    public DataPoint save(String accountName, Account account) {
        Instant instant = LocalDate.now().atStartOfDay()
                .atZone(ZoneId.systemDefault()).toInstant();

        DataPointId pointId = new DataPointId(accountName, Date.from(instant));

        Set<ItemMetric> incomes = account.getIncomes().stream()
                .map(this::createItemMetric)
                .collect(Collectors.toSet());

        Set<ItemMetric> expenses = account.getExpenses().stream()
                .map(this::createItemMetric)
                .collect(Collectors.toSet());

        Map<StatisticMetric, BigDecimal> statistics = createStatisticMetrics(incomes, expenses, account.getSaving());

        DataPoint dataPoint = new DataPoint();
        dataPoint.setId(pointId);
        dataPoint.setIncomes(incomes);
        dataPoint.setExpenses(expenses);
        dataPoint.setStatistics(statistics);
        dataPoint.setRates(exchangeRatesService.getCurrentRates());

        log.debug("new datapoint has been created: {}", pointId);

        return dataPointRepository.save(dataPoint);
    }

    private Map<StatisticMetric, BigDecimal> createStatisticMetrics(Set<ItemMetric> incomes, Set<ItemMetric> expenses, Saving saving) {

        BigDecimal savingAmount = exchangeRatesService.convert(saving.getCurrency(), Currency.getBase(), saving.getAmount());

        BigDecimal expensesAmount = expenses.stream()
                .map(ItemMetric::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal incomesAmount = incomes.stream()
                .map(ItemMetric::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ImmutableMap.of(
                StatisticMetric.EXPENSES_AMOUNT, expensesAmount,
                StatisticMetric.INCOMES_AMOUNT, incomesAmount,
                StatisticMetric.SAVING_AMOUNT, savingAmount
        );
    }

    /**
     * Normalizes given item amount to {@link Currency#getBase()} currency with
     * {@link TimePeriod#getBase()} time period
     */
    private ItemMetric createItemMetric(Item item) {

        BigDecimal amount = exchangeRatesService
                .convert(item.getCurrency(), Currency.getBase(), item.getAmount())
                .divide(item.getPeriod().getBaseRatio(), 4, RoundingMode.HALF_UP);

        return new ItemMetric(item.getTitle(), amount);
    }
}
