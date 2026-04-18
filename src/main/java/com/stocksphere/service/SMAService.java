package com.stocksphere.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * SMA calculations:
 *  - Basic SMA scalar using latest N prices (Task 5)
 *  - Rolling SMA history aligned with price series (chart fix)
 *  - Momentum signal: today vs yesterday (Task 6)
 *  - SMA20/SMA50 crossover strategy (Task 7)
 *  - Configurable periods via application.properties (Task 13)
 */
@Service
public class SMAService {

    private static final Logger log = LoggerFactory.getLogger(SMAService.class);

    @Value("${stocksphere.sma.short:20}")
    private int shortPeriod;

    @Value("${stocksphere.sma.long:50}")
    private int longPeriod;

    /**
     * Calculates SMA scalar using the last {@code period} prices (Task 5).
     * If period > list size, uses all available prices.
     */
    public double calculateSMA(List<Double> prices, int period) {
        if (prices == null || prices.isEmpty()) {
            throw new IllegalArgumentException("Prices list cannot be null or empty");
        }
        if (period <= 0) {
            throw new IllegalArgumentException("Period must be greater than 0");
        }
        int effectivePeriod = Math.min(period, prices.size());
        int start = prices.size() - effectivePeriod;
        List<Double> window = prices.subList(start, prices.size());
        double sum = window.stream().mapToDouble(Double::doubleValue).sum();
        double sma = sum / effectivePeriod;
        double rounded = Math.round(sma * 100.0) / 100.0;
        log.debug("SMA({}) = {}", effectivePeriod, rounded);
        return rounded;
    }

    /**
     * Returns a rolling SMA time-series aligned 1:1 with the prices list.
     * Entries before the first full window are null so the chart line starts
     * only once enough history is available.
     */
    public List<Double> calculateSMAHistory(List<Double> prices, int period) {
        if (prices == null || prices.isEmpty() || period <= 0) {
            return new ArrayList<>();
        }
        int effectivePeriod = Math.min(period, prices.size());
        List<Double> history = new ArrayList<>(prices.size());
        for (int i = 0; i < prices.size(); i++) {
            if (i + 1 < effectivePeriod) {
                history.add(null);
            } else {
                int start = i - effectivePeriod + 1;
                double sum = 0;
                for (int j = start; j <= i; j++) {
                    sum += prices.get(j);
                }
                history.add(Math.round((sum / effectivePeriod) * 100.0) / 100.0);
            }
        }
        return history;
    }

    /**
     * Price vs SMA signal: bullish/bearish/neutral (within ±0.5% band).
     */
    public String getSMASignal(double currentPrice, double sma) {
        double pct = ((currentPrice - sma) / sma) * 100.0;
        if (pct > 0.5)  return "bullish";
        if (pct < -0.5) return "bearish";
        return "neutral";
    }

    /**
     * Momentum signal: today's price vs yesterday's (Task 6).
     */
    public String getMomentumSignal(List<Double> prices) {
        if (prices == null || prices.size() < 2) {
            return "neutral";
        }
        double today     = prices.get(prices.size() - 1);
        double yesterday = prices.get(prices.size() - 2);
        if (today > yesterday) return "bullish";
        if (today < yesterday) return "bearish";
        return "neutral";
    }

    /**
     * SMA crossover strategy: SMA(short) vs SMA(long) (Task 7).
     * Uses the service-level configured periods (strategy config, not per-request).
     */
    public String getCrossoverSignal(List<Double> prices) {
        if (prices == null || prices.size() < longPeriod) {
            log.debug("Not enough data for SMA crossover ({} prices, need {})",
                    prices == null ? 0 : prices.size(), longPeriod);
            return "neutral";
        }
        double smaShort = calculateSMA(prices, shortPeriod);
        double smaLong  = calculateSMA(prices, longPeriod);
        log.debug("Crossover: SMA{} = {}, SMA{} = {}", shortPeriod, smaShort, longPeriod, smaLong);
        if (smaShort > smaLong) return "bullish";
        if (smaShort < smaLong) return "bearish";
        return "neutral";
    }

    public int getShortPeriod() { return shortPeriod; }
    public int getLongPeriod()  { return longPeriod; }
}
