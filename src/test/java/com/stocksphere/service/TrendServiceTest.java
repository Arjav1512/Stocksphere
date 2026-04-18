package com.stocksphere.service;

import com.stocksphere.model.TrendResult;
import com.stocksphere.repository.DataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TrendServiceTest {

    private TrendService trendService;

    @BeforeEach
    void setUp() {
        SMAService       smaService       = new SMAService();
        SentimentService sentimentService = new SentimentService();
        sentimentService.loadDictionary();

        DataLoader   dataLoader   = new DataLoader();
        StockService stockService = new StockService();
        NewsService  newsService  = new NewsService();

        ReflectionTestUtils.setField(smaService,    "shortPeriod", 20);
        ReflectionTestUtils.setField(smaService,    "longPeriod",  50);
        ReflectionTestUtils.setField(stockService,  "dataLoader",  dataLoader);
        ReflectionTestUtils.setField(stockService,  "useMockData", true);

        trendService = new TrendService();
        ReflectionTestUtils.setField(trendService, "smaService",       smaService);
        ReflectionTestUtils.setField(trendService, "sentimentService",  sentimentService);
        ReflectionTestUtils.setField(trendService, "newsService",       newsService);
        ReflectionTestUtils.setField(trendService, "stockService",      stockService);
        ReflectionTestUtils.setField(trendService, "shortPeriod",       20);
    }

    // ── Core result validity ───────────────────────────────────────────────────

    @Test
    void testAnalyzeReturnsResult() {
        assertNotNull(trendService.analyze("RELIANCE", null));
    }

    @Test
    void testAnalyzeSymbolUppercased() {
        assertEquals("TCS", trendService.analyze("tcs", null).getSymbol());
    }

    @Test
    void testAnalyzeHasPrices() {
        TrendResult result = trendService.analyze("INFY", null);
        assertNotNull(result.getPrices());
        assertTrue(result.getPrices().size() >= 30);
    }

    @Test
    void testAnalyzeHasSMA20() {
        TrendResult result = trendService.analyze("AAPL", null);
        assertNotNull(result.getSma20());
        assertTrue(result.getSma20() > 0);
    }

    @Test
    void testAnalyzeHasSMA50() {
        TrendResult result = trendService.analyze("MSFT", null);
        assertNotNull(result.getSma50());
        assertTrue(result.getSma50() > 0);
    }

    @Test
    void testAnalyzeTrendIsValid() {
        String trend = trendService.analyze("MSFT", null).getTrend();
        assertTrue(trend.equals("BULLISH") || trend.equals("BEARISH") || trend.equals("NEUTRAL"));
    }

    @Test
    void testAnalyzeHasArticles() {
        assertTrue(trendService.analyze("TSLA", null).getArticles().size() >= 5);
    }

    @Test
    void testAnalyzeHasFiveSignalVotes() {
        TrendResult result = trendService.analyze("RELIANCE", null);
        assertNotNull(result.getSignalVotes());
        assertEquals(5, result.getSignalVotes().size());
        assertTrue(result.getSignalVotes().containsKey("SMA"));
        assertTrue(result.getSignalVotes().containsKey("Crossover"));
        assertTrue(result.getSignalVotes().containsKey("Momentum"));
        assertTrue(result.getSignalVotes().containsKey("Volume"));
        assertTrue(result.getSignalVotes().containsKey("Sentiment"));
    }

    @Test
    void testAnalyzeSentimentNormalizedInRange() {
        double norm = trendService.analyze("RELIANCE", null).getSentimentNormalized();
        assertTrue(norm >= -1.0 && norm <= 1.0);
    }

    @Test
    void testAnalyzeSentimentClassification() {
        String sentiment = trendService.analyze("RELIANCE", null).getSentiment();
        assertTrue(sentiment.equals("positive") || sentiment.equals("negative") || sentiment.equals("neutral"));
    }

    @Test
    void testAnalyzeHasCrossoverSignal() {
        String signal = trendService.analyze("AAPL", null).getCrossoverSignal();
        assertNotNull(signal);
        assertTrue(signal.equals("bullish") || signal.equals("bearish") || signal.equals("neutral"));
    }

    @Test
    void testAnalyzeHasMomentumSignal() {
        String signal = trendService.analyze("TSLA", null).getMomentumSignal();
        assertNotNull(signal);
        assertTrue(signal.equals("bullish") || signal.equals("bearish") || signal.equals("neutral"));
    }

    @Test
    void testAnalyzeHasSummary() {
        String summary = trendService.analyze("INFY", null).getSummary();
        assertNotNull(summary);
        assertFalse(summary.isBlank());
    }

    // ── Confidence bounds (was broken: could exceed 100) ──────────────────────

    @Test
    void testConfidenceAlwaysInRange() {
        List<String> symbols = List.of("RELIANCE","TCS","INFY","HDFCBANK","WIPRO",
                                       "AAPL","MSFT","TSLA","NVDA","META");
        for (String sym : symbols) {
            double conf = trendService.analyze(sym, null).getConfidence();
            assertTrue(conf >= 0.0 && conf <= 100.0,
                    "Confidence out of [0,100] for " + sym + ": " + conf);
        }
    }

    @Test
    void testConfidenceIsNotNegative() {
        double conf = trendService.analyze("RELIANCE", null).getConfidence();
        assertTrue(conf >= 0.0);
    }

    @Test
    void testConfidenceDoesNotExceed100() {
        double conf = trendService.analyze("RELIANCE", null).getConfidence();
        assertTrue(conf <= 100.0);
    }

    // ── smaPeriod override (was broken: parameter silently ignored) ────────────

    @Test
    void testSmaPeriodDefaultIsUsedWhenNull() {
        TrendResult result = trendService.analyze("RELIANCE", null);
        assertEquals(20, result.getEffectiveSmaPeriod());
    }

    @Test
    void testSmaPeriodOverrideIsHonored() {
        TrendResult r5 = trendService.analyze("RELIANCE", 5);
        assertEquals(5, r5.getEffectiveSmaPeriod());
    }

    @Test
    void testSmaPeriodOverrideChangesSMA20Value() {
        TrendResult r5  = trendService.analyze("RELIANCE", 5);
        TrendResult r20 = trendService.analyze("RELIANCE", null);
        // SMA over last 5 prices vs last 20 prices of the same dataset must differ
        assertNotEquals(r5.getSma20(), r20.getSma20(),
                "smaPeriod=5 must produce a different SMA20 scalar than smaPeriod=20");
    }

    @Test
    void testSmaPeriodReflectedInSummary() {
        TrendResult result = trendService.analyze("AAPL", 10);
        assertNotNull(result.getSummary());
        assertTrue(result.getSummary().contains("SMA(10)"),
                "Summary should reference the effective SMA period. Got: " + result.getSummary());
    }

    // ── Volume signal (was broken: hash-based, not data-driven) ──────────────

    @Test
    void testVolumeSignalIsValidValue() {
        String signal = trendService.analyze("RELIANCE", null).getVolumeSignal();
        assertTrue(List.of("bullish", "bearish", "neutral").contains(signal));
    }

    @Test
    void testVolumeSignalPresentInSignalVotes() {
        TrendResult result = trendService.analyze("TCS", null);
        assertTrue(result.getSignalVotes().containsKey("Volume"));
        String vote = result.getSignalVotes().get("Volume");
        assertTrue(List.of("bullish", "bearish", "neutral").contains(vote));
    }

    // ── Rolling SMA history ────────────────────────────────────────────────────

    @Test
    void testSma20HistoryLengthMatchesPrices() {
        TrendResult result = trendService.analyze("RELIANCE", null);
        assertNotNull(result.getSma20History());
        assertEquals(result.getPrices().size(), result.getSma20History().size());
    }

    @Test
    void testSma50HistoryLengthMatchesPrices() {
        TrendResult result = trendService.analyze("RELIANCE", null);
        assertNotNull(result.getSma50History());
        assertEquals(result.getPrices().size(), result.getSma50History().size());
    }

    @Test
    void testSma20HistoryFirstEntriesAreNull() {
        TrendResult result = trendService.analyze("RELIANCE", null);
        List<Double> history = result.getSma20History();
        // First 19 entries (indices 0-18) should be null for period=20
        for (int i = 0; i < 19; i++) {
            assertNull(history.get(i), "sma20History[" + i + "] should be null");
        }
    }

    @Test
    void testSma20HistoryHasNonNullValuesAfterPeriod() {
        TrendResult result = trendService.analyze("RELIANCE", null);
        List<Double> history = result.getSma20History();
        // Index 19 onward should all be non-null
        for (int i = 19; i < history.size(); i++) {
            assertNotNull(history.get(i), "sma20History[" + i + "] should not be null");
            assertTrue(history.get(i) > 0);
        }
    }

    @Test
    void testSma20HistoryLastEntryMatchesSma20Scalar() {
        TrendResult result = trendService.analyze("AAPL", null);
        List<Double> history = result.getSma20History();
        double lastHistoryValue = history.get(history.size() - 1);
        assertEquals(result.getSma20(), lastHistoryValue, 0.01);
    }

    @Test
    void testSma50HistoryFirstEntriesAreNull() {
        TrendResult result = trendService.analyze("RELIANCE", null);
        List<Double> history = result.getSma50History();
        // First 49 entries (indices 0-48) should be null for period=50
        for (int i = 0; i < 49; i++) {
            assertNull(history.get(i), "sma50History[" + i + "] should be null");
        }
    }

    @Test
    void testSma50HistoryLastEntryMatchesSma50Scalar() {
        TrendResult result = trendService.analyze("MSFT", null);
        List<Double> history = result.getSma50History();
        double lastHistoryValue = history.get(history.size() - 1);
        assertEquals(result.getSma50(), lastHistoryValue, 0.01);
    }

    @Test
    void testCustomSmaPeriodHistoryHasFewerNulls() {
        TrendResult r5  = trendService.analyze("RELIANCE", 5);
        TrendResult r20 = trendService.analyze("RELIANCE", null);
        long nullsIn5  = r5.getSma20History().stream().filter(v -> v == null).count();
        long nullsIn20 = r20.getSma20History().stream().filter(v -> v == null).count();
        assertTrue(nullsIn5 < nullsIn20,
                "SMA period 5 should produce fewer nulls in history than period 20");
    }
}
