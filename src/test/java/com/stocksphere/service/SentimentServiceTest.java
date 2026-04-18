package com.stocksphere.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SentimentServiceTest {

    private SentimentService sentimentService;

    @BeforeEach
    void setUp() {
        sentimentService = new SentimentService();
        sentimentService.loadDictionary();
    }

    // ── Basic polarity ─────────────────────────────────────────────────────────

    @Test
    void testPositiveSentiment() {
        double score = sentimentService.analyzeSentiment("Reliance reports strong profit growth and record revenue");
        assertTrue(score > 0, "Positive text should yield positive score, got: " + score);
    }

    @Test
    void testNegativeSentiment() {
        double score = sentimentService.analyzeSentiment("Company faces losses decline and weak earnings miss");
        assertTrue(score < 0, "Negative text should yield negative score, got: " + score);
    }

    @Test
    void testNeutralSentiment() {
        double score = sentimentService.analyzeSentiment("Company announces quarterly results");
        assertEquals(0.0, score, "Neutral text without sentiment words should score 0");
    }

    @Test
    void testNullTextReturnsZero() {
        assertEquals(0.0, sentimentService.analyzeSentiment(null));
    }

    @Test
    void testEmptyTextReturnsZero() {
        assertEquals(0.0, sentimentService.analyzeSentiment(""));
    }

    // ── Text normalisation (Task 4) ────────────────────────────────────────────

    @Test
    void testPunctuationStripped_growth() {
        // "growth", "growth!", "growth." should all give the same score
        double plain  = sentimentService.analyzeSentiment("growth");
        double excl   = sentimentService.analyzeSentiment("growth!");
        double period = sentimentService.analyzeSentiment("growth.");
        assertEquals(plain, excl,   0.001, "growth == growth!");
        assertEquals(plain, period, 0.001, "growth == growth.");
    }

    @Test
    void testPunctuationStripped_profit() {
        double a = sentimentService.analyzeSentiment("profit");
        double b = sentimentService.analyzeSentiment("profit,");
        double c = sentimentService.analyzeSentiment("profit?");
        assertEquals(a, b, 0.001);
        assertEquals(a, c, 0.001);
    }

    @Test
    void testCaseNormalization() {
        double lower = sentimentService.analyzeSentiment("profit");
        double upper = sentimentService.analyzeSentiment("PROFIT");
        double mixed = sentimentService.analyzeSentiment("Profit");
        assertEquals(lower, upper, 0.001, "Case should be normalised");
        assertEquals(lower, mixed, 0.001, "Case should be normalised");
    }

    // ── Classification ─────────────────────────────────────────────────────────

    @Test
    void testClassifyPositive() {
        assertEquals("positive", sentimentService.classifySentiment(3.0));
    }

    @Test
    void testClassifyNegative() {
        assertEquals("negative", sentimentService.classifySentiment(-3.0));
    }

    @Test
    void testClassifyNeutral() {
        assertEquals("neutral", sentimentService.classifySentiment(0.5));
    }

    // ── Signal ─────────────────────────────────────────────────────────────────

    @Test
    void testSentimentSignalBullish() {
        assertEquals("bullish", sentimentService.getSentimentSignal(2.0));
    }

    @Test
    void testSentimentSignalBearish() {
        assertEquals("bearish", sentimentService.getSentimentSignal(-2.0));
    }

    // ── Normalised score (Task 8) ──────────────────────────────────────────────

    @Test
    void testNormalizedScoreInRange() {
        // Feed a strongly positive text and confirm normalised score is within [-1, +1]
        double raw        = sentimentService.analyzeSentiment("profit growth surge rally gain win boom");
        double normalised = sentimentService.normalizeSentimentScore(raw);
        assertTrue(normalised >= -1.0 && normalised <= 1.0,
                "Normalised score must be within [-1, 1], got: " + normalised);
    }

    @Test
    void testNormalizedScorePositiveForPositiveText() {
        double raw        = sentimentService.analyzeSentiment("profit growth record");
        double normalised = sentimentService.normalizeSentimentScore(raw);
        assertTrue(normalised > 0, "Positive text should give positive normalised score");
    }

    @Test
    void testNormalizedScoreNegativeForNegativeText() {
        double raw        = sentimentService.analyzeSentiment("loss decline crash collapse");
        double normalised = sentimentService.normalizeSentimentScore(raw);
        assertTrue(normalised < 0, "Negative text should give negative normalised score");
    }

    @Test
    void testNormalizedScoreClampedAtMax() {
        // Extremely positive raw score should clamp to +1
        double normalised = sentimentService.normalizeSentimentScore(1000.0);
        assertEquals(1.0, normalised, "Extreme positive score should clamp to 1.0");
    }

    @Test
    void testNormalizedScoreClampedAtMin() {
        double normalised = sentimentService.normalizeSentimentScore(-1000.0);
        assertEquals(-1.0, normalised, "Extreme negative score should clamp to -1.0");
    }

    @Test
    void testKnownPositiveText() {
        // "strong"(+2) + "profit"(+2) + "growth"(+2) = 6
        double score = sentimentService.analyzeSentiment("Reliance reports strong profit growth");
        assertEquals(6.0, score, 0.01);
    }
}
