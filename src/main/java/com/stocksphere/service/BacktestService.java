package com.stocksphere.service;

import com.stocksphere.dto.BacktestResponse;
import com.stocksphere.model.StockData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Task 14 — Backtesting Feature.
 *
 * Runs the SMA crossover + momentum strategy across the available historical
 * price window and measures how often the predicted direction matched the
 * actual next-day price move.
 */
@Service
public class BacktestService {

    private static final Logger log = LoggerFactory.getLogger(BacktestService.class);

    @Autowired private StockService stockService;
    @Autowired private SMAService   smaService;

    @Value("${stocksphere.sma.short:20}")
    private int shortPeriod;

    @Value("${stocksphere.sma.long:50}")
    private int longPeriod;

    /**
     * Backtests the SMA crossover + momentum combo strategy on historical data.
     *
     * @param symbol stock symbol
     * @return BacktestResponse with accuracy stats
     */
    public BacktestResponse runBacktest(String symbol) {
        log.info("Running backtest for {}", symbol);

        StockData stockData = stockService.getStockData(symbol);
        List<Double> prices = stockData.getClosingPrices();

        int minRequiredPrices = longPeriod + 2;  // need enough for SMA50 + lookahead
        if (prices.size() < minRequiredPrices) {
            log.warn("Not enough price data for {} to backtest (have {}, need {})",
                    symbol, prices.size(), minRequiredPrices);
            BacktestResponse resp = new BacktestResponse();
            resp.setSymbol(symbol.toUpperCase());
            resp.setTotalPredictions(0);
            resp.setCorrectPredictions(0);
            resp.setAccuracyPercentage(0);
            resp.setStrategy("SMA Crossover + Momentum");
            resp.setNote("Insufficient price data for backtesting (need at least " + minRequiredPrices + " days)");
            return resp;
        }

        int total   = 0;
        int correct = 0;

        // Walk forward: for each candle where we have enough history,
        // predict direction and compare to actual next-day move.
        for (int i = longPeriod; i < prices.size() - 1; i++) {
            List<Double> window = prices.subList(0, i + 1);

            // Strategy signal
            String crossover = smaService.getCrossoverSignal(window);
            String momentum  = smaService.getMomentumSignal(window);

            String prediction = fuseSignals(crossover, momentum);
            if (prediction.equals("neutral")) continue;   // skip uncertain days

            // Actual next-day move
            double today    = prices.get(i);
            double tomorrow = prices.get(i + 1);
            String actual   = tomorrow > today ? "bullish" : "bearish";

            if (prediction.equals(actual)) correct++;
            total++;
        }

        double accuracy = total == 0 ? 0 : Math.round((correct * 100.0 / total) * 10.0) / 10.0;
        log.info("Backtest {}: {}/{} correct → {}%", symbol, correct, total, accuracy);

        BacktestResponse resp = new BacktestResponse();
        resp.setSymbol(symbol.toUpperCase());
        resp.setTotalPredictions(total);
        resp.setCorrectPredictions(correct);
        resp.setAccuracyPercentage(accuracy);
        resp.setStrategy("SMA Crossover + Momentum");
        resp.setNote("Walk-forward backtest over " + prices.size() + " historical trading days");
        return resp;
    }

    private String fuseSignals(String crossover, String momentum) {
        if (crossover.equals("bullish") && momentum.equals("bullish")) return "bullish";
        if (crossover.equals("bearish") && momentum.equals("bearish")) return "bearish";
        return "neutral";
    }
}
