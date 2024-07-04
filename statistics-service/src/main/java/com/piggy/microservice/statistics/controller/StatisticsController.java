package com.piggy.microservice.statistics.controller;

import com.piggy.microservice.statistics.domain.Account;
import com.piggy.microservice.statistics.domain.timeseries.DataPoint;
import com.piggy.microservice.statistics.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @Autowired
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public List<DataPoint> getCurrentAccountStatistics(@PathVariable String name) {
        return statisticsService.findByAccountName(name);
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.PUT)
    public String updateAccountStatistics(@PathVariable String name, @Valid @RequestBody Account account) {
        statisticsService.save(name, account);
        return ("Account: " + name + " has been updated.");
    }


}
