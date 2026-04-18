package com.stocksphere.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Sentiment analysis service.
 * Task 4  — normalise text: lowercase + strip punctuation so "growth", "growth!", "growth." match.
 * Task 8  — return normalised sentiment score in range [-1, +1].
 * Task 12 — SLF4J logging.
 */
@Service
public class SentimentService {

    private static final Logger log = LoggerFactory.getLogger(SentimentService.class);

    /** Maximum absolute raw score used to normalise to [-1, +1] */
    private static final double NORMALISE_SCALE = 10.0;

    private Map<String, Integer> sentimentDictionary = new HashMap<>();

    @PostConstruct
    public void loadDictionary() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("sentiment-dictionary.txt")) {
            if (is == null) {
                log.warn("sentiment-dictionary.txt not found on classpath — using built-in dictionary");
                loadBuiltinDictionary();
                return;
            }
            int count = 0;
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    try {
                        String word = parts[0].trim().toLowerCase();
                        String scoreStr = parts[1].trim()
                                .replace("\u2212", "-")   // unicode minus
                                .replace("\u2013", "-");  // en-dash
                        int score = Integer.parseInt(scoreStr);
                        sentimentDictionary.put(word, score);
                        count++;
                    } catch (NumberFormatException ignored) { /* skip malformed */ }
                }
            }
            log.info("Loaded {} words from sentiment-dictionary.txt", count);
        } catch (IOException e) {
            log.error("Error reading sentiment dictionary: {} — falling back to built-in", e.getMessage());
            loadBuiltinDictionary();
        }
    }

    private void loadBuiltinDictionary() {
        String[] positives = {"profit","growth","surge","rally","gain","strong","record","beat",
                "outperform","bullish","upgrade","expansion","revenue","earnings","increase",
                "rise","recovery","improved","excellent","success","optimistic","dividend","win","boom","soar"};
        String[] negatives = {"loss","decline","fall","drop","weak","miss","underperform","bearish",
                "downgrade","contraction","debt","deficit","concern","risk","warning","struggle",
                "uncertain","slowdown","recession","crisis","crash","collapse","plunge","tumble","selloff"};
        for (String w : positives) sentimentDictionary.put(w, 2);
        for (String w : negatives) sentimentDictionary.put(w, -2);
        log.info("Built-in dictionary loaded ({} words)", sentimentDictionary.size());
    }

    /**
     * Analyses text sentiment.
     * Task 4: normalise text — lowercase + remove all non-alpha characters.
     *
     * @return raw integer score (positive = bullish, negative = bearish)
     */
    public double analyzeSentiment(String text) {
        if (text == null || text.isEmpty()) return 0.0;

        // Task 4: lowercase + strip punctuation, so "growth!" == "growth"
        String[] words = text.toLowerCase()
                .replaceAll("[^a-z ]", " ")   // keep only letters and spaces
                .split("\\s+");

        int totalScore = 0;
        int matchCount = 0;
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (sentimentDictionary.containsKey(word)) {
                totalScore += sentimentDictionary.get(word);
                matchCount++;
            }
        }
        log.debug("Sentiment '{}…' → raw={}, matches={}", text.length() > 40 ? text.substring(0,40) : text, totalScore, matchCount);
        return totalScore;
    }

    /**
     * Task 8: normalise raw score to [-1, +1] range.
     * Clamped so extreme scores don't exceed bounds.
     */
    public double normalizeSentimentScore(double rawScore) {
        double normalised = rawScore / NORMALISE_SCALE;
        return Math.max(-1.0, Math.min(1.0, Math.round(normalised * 100.0) / 100.0));
    }

    public String classifySentiment(double score) {
        if (score > 1)  return "positive";
        if (score < -1) return "negative";
        return "neutral";
    }

    public String getSentimentSignal(double score) {
        if (score > 0) return "bullish";
        if (score < 0) return "bearish";
        return "neutral";
    }
}
