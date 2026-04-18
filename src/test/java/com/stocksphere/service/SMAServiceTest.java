package com.stocksphere.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SMAServiceTest {

    private SMAService smaService;

    @BeforeEach
    void setUp() {
        smaService = new SMAService();
        ReflectionTestUtils.setField(smaService, "shortPeriod", 20);
        ReflectionTestUtils.setField(smaService, "longPeriod",  50);
    }

    // ── Basic SMA (Task 5 — use last N prices) ─────────────────────────────────

    @Test
    void testBasicSMACalculation() {
        List<Double> prices = Arrays.asList(100.0, 102.0, 104.0, 106.0, 108.0);
        double sma = smaService.calculateSMA(prices, 5);
        assertEquals(104.0, sma, 0.01);
    }

    @Test
    void testSMAUsesLastNPrices() {
        List<Double> prices = Arrays.asList(100.0, 102.0, 104.0, 106.0, 108.0, 110.0);
        double sma = smaService.calculateSMA(prices, 3);
        assertEquals(108.0, sma, 0.01);
    }

    @Test
    void testSMAWithPeriodLargerThanList() {
        List<Double> prices = Arrays.asList(100.0, 200.0);
        double sma = smaService.calculateSMA(prices, 10);
        assertEquals(150.0, sma, 0.01);
    }

    @Test
    void testSMASignalBullish() {
        assertEquals("bullish", smaService.getSMASignal(110.0, 100.0));
    }

    @Test
    void testSMASignalBearish() {
        assertEquals("bearish", smaService.getSMASignal(90.0, 100.0));
    }

    @Test
    void testSMASignalNeutral() {
        assertEquals("neutral", smaService.getSMASignal(100.1, 100.0));
    }

    // ── Momentum (Task 6 — today vs yesterday) ─────────────────────────────────

    @Test
    void testMomentumSignalBullishTodayHigher() {
        List<Double> prices = Arrays.asList(100.0, 102.0, 104.0, 100.0, 105.0);
        assertEquals("bullish", smaService.getMomentumSignal(prices));
    }

    @Test
    void testMomentumSignalBearishTodayLower() {
        List<Double> prices = Arrays.asList(100.0, 102.0, 104.0, 100.0, 95.0);
        assertEquals("bearish", smaService.getMomentumSignal(prices));
    }

    @Test
    void testMomentumSignalNeutralSamePrice() {
        List<Double> prices = Arrays.asList(100.0, 100.0);
        assertEquals("neutral", smaService.getMomentumSignal(prices));
    }

    @Test
    void testMomentumSignalNeutralSinglePrice() {
        List<Double> prices = Arrays.asList(100.0);
        assertEquals("neutral", smaService.getMomentumSignal(prices));
    }

    // ── SMA Crossover (Task 7) ─────────────────────────────────────────────────

    @Test
    void testCrossoverNeutralWhenInsufficientData() {
        List<Double> prices = generatePrices(30, 100.0);
        assertEquals("neutral", smaService.getCrossoverSignal(prices));
    }

    @Test
    void testCrossoverBullishWhenSMA20AboveSMA50() {
        List<Double> prices = generateTrendingPrices(60, 100.0, 2.0);
        assertEquals("bullish", smaService.getCrossoverSignal(prices));
    }

    @Test
    void testCrossoverBearishWhenSMA20BelowSMA50() {
        List<Double> prices = generateTrendingPrices(60, 200.0, -2.0);
        assertEquals("bearish", smaService.getCrossoverSignal(prices));
    }

    // ── Error cases ────────────────────────────────────────────────────────────

    @Test
    void testSMAWithNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> smaService.calculateSMA(null, 5));
    }

    @Test
    void testSMAWithZeroPeriodThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> smaService.calculateSMA(Arrays.asList(100.0, 200.0), 0));
    }

    @Test
    void testSMAWithNegativePeriodThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> smaService.calculateSMA(Arrays.asList(100.0, 200.0), -1));
    }

    // ── Rolling SMA History ────────────────────────────────────────────────────

    @Test
    void testSMAHistoryLengthMatchesPrices() {
        List<Double> prices = generatePrices(60, 100.0);
        List<Double> history = smaService.calculateSMAHistory(prices, 20);
        assertEquals(60, history.size());
    }

    @Test
    void testSMAHistoryFirstEntriesAreNull() {
        List<Double> prices = generatePrices(60, 100.0);
        List<Double> history = smaService.calculateSMAHistory(prices, 20);
        for (int i = 0; i < 19; i++) {
            assertNull(history.get(i), "Entry at index " + i + " should be null");
        }
    }

    @Test
    void testSMAHistoryFirstFullEntryIsCorrect() {
        List<Double> prices = generatePrices(60, 100.0);
        List<Double> history = smaService.calculateSMAHistory(prices, 20);
        assertNotNull(history.get(19));
        assertEquals(100.0, history.get(19), 0.01);
    }

    @Test
    void testSMAHistoryLastEntryMatchesCalculateSMA() {
        List<Double> prices = generateTrendingPrices(60, 100.0, 1.0);
        List<Double> history = smaService.calculateSMAHistory(prices, 20);
        double expectedScalar = smaService.calculateSMA(prices, 20);
        assertEquals(expectedScalar, history.get(59), 0.01);
    }

    @Test
    void testSMAHistoryAllNullWhenPeriodEqualsSize() {
        List<Double> prices = generatePrices(20, 100.0);
        List<Double> history = smaService.calculateSMAHistory(prices, 20);
        assertEquals(20, history.size());
        // Only last entry should be non-null (index 19 = first full window)
        for (int i = 0; i < 19; i++) {
            assertNull(history.get(i));
        }
        assertNotNull(history.get(19));
    }

    @Test
    void testSMAHistoryEmptyInputReturnsEmpty() {
        List<Double> history = smaService.calculateSMAHistory(List.of(), 20);
        assertTrue(history.isEmpty());
    }

    @Test
    void testSMAHistoryNullInputReturnsEmpty() {
        List<Double> history = smaService.calculateSMAHistory(null, 20);
        assertTrue(history.isEmpty());
    }

    @Test
    void testSMAHistoryZeroPeriodReturnsEmpty() {
        List<Double> prices = generatePrices(10, 100.0);
        List<Double> history = smaService.calculateSMAHistory(prices, 0);
        assertTrue(history.isEmpty());
    }

    @Test
    void testSMAHistoryPeriodLargerThanPricesUsesAllPrices() {
        // period=50, only 30 prices — effectivePeriod=30, only last entry should be non-null
        List<Double> prices = generatePrices(30, 150.0);
        List<Double> history = smaService.calculateSMAHistory(prices, 50);
        assertEquals(30, history.size());
        for (int i = 0; i < 29; i++) {
            assertNull(history.get(i));
        }
        assertNotNull(history.get(29));
        assertEquals(150.0, history.get(29), 0.01);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private List<Double> generatePrices(int count, double base) {
        Double[] arr = new Double[count];
        for (int i = 0; i < count; i++) arr[i] = base;
        return Arrays.asList(arr);
    }

    private List<Double> generateTrendingPrices(int count, double start, double step) {
        Double[] arr = new Double[count];
        double price = start;
        for (int i = 0; i < count; i++) {
            arr[i] = price;
            price += step;
        }
        return Arrays.asList(arr);
    }
}
