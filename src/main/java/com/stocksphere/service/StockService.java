package com.stocksphere.service;

import com.stocksphere.model.StockData;
import com.stocksphere.repository.DataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Task 12: SLF4J logging added.
 */
@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    @Autowired
    private DataLoader dataLoader;

    @Value("${app.use-mock-data:true}")
    private boolean useMockData;

    public StockData getStockData(String symbol) {
        log.info("Loading stock data for symbol: {}", symbol);
        if (useMockData) {
            StockData data = dataLoader.loadMockStockData(symbol);
            log.debug("Loaded {} price points for {}", data.getClosingPrices().size(), symbol);
            return data;
        }
        // Production: call Alpha Vantage / Yahoo Finance here
        return dataLoader.loadMockStockData(symbol);
    }
}
