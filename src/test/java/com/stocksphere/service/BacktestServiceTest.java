package com.stocksphere.service;

import com.stocksphere.dto.BacktestResponse;
import com.stocksphere.repository.DataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BacktestServiceTest {

    private BacktestService backtestService;

    @BeforeEach
    void setUp() {
        SMAService smaService = new SMAService();
        ReflectionTestUtils.setField(smaService, "shortPeriod", 20);
        ReflectionTestUtils.setField(smaService, "longPeriod",  50);

        DataLoader   dataLoader   = new DataLoader();
        StockService stockService = new StockService();
        ReflectionTestUtils.setField(stockService, "dataLoader",  dataLoader);
        ReflectionTestUtils.setField(stockService, "useMockData", true);

        backtestService = new BacktestService();
        ReflectionTestUtils.setField(backtestService, "stockService", stockService);
        ReflectionTestUtils.setField(backtestService, "smaService",   smaService);
        ReflectionTestUtils.setField(backtestService, "shortPeriod",  20);
        ReflectionTestUtils.setField(backtestService, "longPeriod",   50);
    }

    // ── Result not null ────────────────────────────────────────────────────────

    @Test
    void testBacktestReturnsNonNullResult() {
        assertNotNull(backtestService.runBacktest("RELIANCE"));
    }

    // ── Symbol handling ────────────────────────────────────────────────────────

    @Test
    void testBacktestSymbolIsUppercased() {
        assertEquals("RELIANCE", backtestService.runBacktest("reliance").getSymbol());
    }

    @Test
    void testBacktestSymbolPreservedInResponse() {
        BacktestResponse resp = backtestService.runBacktest("AAPL");
        assertEquals("AAPL", resp.getSymbol());
    }

    // ── Accuracy bounds ────────────────────────────────────────────────────────

    @Test
    void testBacktestAccuracyIsInRange() {
        double accuracy = backtestService.runBacktest("RELIANCE").getAccuracyPercentage();
        assertTrue(accuracy >= 0.0 && accuracy <= 100.0,
                "Accuracy must be in [0, 100], got: " + accuracy);
    }

    @Test
    void testBacktestAccuracyRangeForMultipleSymbols() {
        List<String> symbols = List.of("TCS", "INFY", "AAPL", "MSFT", "TSLA");
        for (String sym : symbols) {
            double acc = backtestService.runBacktest(sym).getAccuracyPercentage();
            assertTrue(acc >= 0.0 && acc <= 100.0,
                    "Accuracy out of range for " + sym + ": " + acc);
        }
    }

    // ── Prediction counts ──────────────────────────────────────────────────────

    @Test
    void testCorrectPredictionsNotGreaterThanTotal() {
        BacktestResponse resp = backtestService.runBacktest("RELIANCE");
        assertTrue(resp.getCorrectPredictions() <= resp.getTotalPredictions(),
                "Correct cannot exceed total predictions");
    }

    @Test
    void testPredictionCountsAreNonNegative() {
        BacktestResponse resp = backtestService.runBacktest("TCS");
        assertTrue(resp.getTotalPredictions() >= 0);
        assertTrue(resp.getCorrectPredictions() >= 0);
    }

    // ── Strategy metadata ─────────────────────────────────────────────────────

    @Test
    void testBacktestStrategyIsPresent() {
        BacktestResponse resp = backtestService.runBacktest("NVDA");
        assertNotNull(resp.getStrategy());
        assertFalse(resp.getStrategy().isBlank());
    }

    @Test
    void testBacktestNoteIsPresent() {
        BacktestResponse resp = backtestService.runBacktest("META");
        assertNotNull(resp.getNote());
        assertFalse(resp.getNote().isBlank());
    }

    // ── Accuracy consistency ───────────────────────────────────────────────────

    @Test
    void testAccuracyIsConsistentWithCounts() {
        BacktestResponse resp = backtestService.runBacktest("AAPL");
        if (resp.getTotalPredictions() == 0) {
            assertEquals(0.0, resp.getAccuracyPercentage(), 0.001);
        } else {
            double expected = Math.round(
                    (resp.getCorrectPredictions() * 100.0 / resp.getTotalPredictions()) * 10.0) / 10.0;
            assertEquals(expected, resp.getAccuracyPercentage(), 0.15,
                    "Reported accuracy should match correct/total ratio");
        }
    }

    // ── Determinism ───────────────────────────────────────────────────────────

    @Test
    void testBacktestIsDeterministic() {
        BacktestResponse first  = backtestService.runBacktest("RELIANCE");
        BacktestResponse second = backtestService.runBacktest("RELIANCE");
        assertEquals(first.getAccuracyPercentage(), second.getAccuracyPercentage(), 0.001,
                "Same symbol must always produce the same backtest result");
        assertEquals(first.getTotalPredictions(), second.getTotalPredictions());
    }
}
