package com.stocksphere.service;

import com.stocksphere.model.NewsArticle;
import com.stocksphere.model.StockData;
import com.stocksphere.model.TrendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Full multi-signal trend analysis.
 * Task 6  — momentum: today > yesterday
 * Task 7  — SMA crossover SMA20 vs SMA50
 * Task 8  — normalised sentiment score [-1, +1]
 * Task 12 — SLF4J logging
 * Task 13 — configurable SMA periods
 */
@Service
public class TrendService {

    private static final Logger log = LoggerFactory.getLogger(TrendService.class);

    @Autowired private SMAService       smaService;
    @Autowired private SentimentService sentimentService;
    @Autowired private NewsService      newsService;
    @Autowired private StockService     stockService;

    @Value("${stocksphere.sma.short:20}")
    private int shortPeriod;

    /**
     * Runs full multi-signal trend analysis.
     *
     * @param symbol    stock ticker
     * @param smaPeriod optional per-request SMA period override (uses configured default if null)
     */
    public TrendResult analyze(String symbol, Integer smaPeriod) {
        log.info("Starting analysis for symbol: {}", symbol);

        int effectiveShortPeriod = (smaPeriod != null) ? smaPeriod : this.shortPeriod;
        TrendResult result = new TrendResult();
        result.setSymbol(symbol.toUpperCase());
        result.setEffectiveSmaPeriod(effectiveShortPeriod);

        // ── Step 1: Stock Data ───────────────────────────────────────────────
        StockData stockData = stockService.getStockData(symbol);
        List<Double> prices = stockData.getClosingPrices();
        result.setPrices(prices);
        result.setDates(stockData.getDates());
        result.setCurrentPrice(stockData.getCurrentPrice());
        log.debug("{}: {} price points, currentPrice={}", symbol, prices.size(), stockData.getCurrentPrice());

        // ── Step 2: SMA scalars ──────────────────────────────────────────────
        double sma20 = smaService.calculateSMA(prices, effectiveShortPeriod);
        result.setSma20(sma20);
        result.setSma(sma20);

        double sma50 = smaService.calculateSMA(prices, smaService.getLongPeriod());
        result.setSma50(sma50);

        // ── Step 3: Rolling SMA history for chart ────────────────────────────
        result.setSma20History(smaService.calculateSMAHistory(prices, effectiveShortPeriod));
        result.setSma50History(smaService.calculateSMAHistory(prices, smaService.getLongPeriod()));

        // ── Step 4: Signal 1 — Price vs SMA ─────────────────────────────────
        String smaSignal = smaService.getSMASignal(stockData.getCurrentPrice(), sma20);
        result.setSmaSignal(smaSignal);
        log.debug("{}: SMA signal = {}", symbol, smaSignal);

        // ── Step 5: Signal 2 — Momentum (today vs yesterday) ────────────────
        String momentumSignal = smaService.getMomentumSignal(prices);
        result.setMomentumSignal(momentumSignal);
        log.debug("{}: Momentum signal = {}", symbol, momentumSignal);

        // ── Step 6: Signal 3 — SMA Crossover ────────────────────────────────
        String crossoverSignal = smaService.getCrossoverSignal(prices);
        result.setCrossoverSignal(crossoverSignal);
        log.debug("{}: Crossover signal = {} (SMA{}={} vs SMA{}={})",
                symbol, crossoverSignal, effectiveShortPeriod, sma20, smaService.getLongPeriod(), sma50);

        // ── Step 7: Signal 4 — Volume ────────────────────────────────────────
        String volumeSignal = getVolumeSignal(stockData);
        result.setVolumeSignal(volumeSignal);
        log.debug("{}: Volume signal = {}", symbol, volumeSignal);

        // ── Step 8: News + Sentiment ─────────────────────────────────────────
        List<NewsArticle> articles = newsService.getNewsArticles(symbol);
        result.setArticles(articles);
        log.debug("{}: {} news articles", symbol, articles.size());

        double totalRawScore = 0;
        for (NewsArticle article : articles) {
            String text = (article.getTitle() != null ? article.getTitle() : "") + " "
                    + (article.getDescription() != null ? article.getDescription() : "");
            totalRawScore += sentimentService.analyzeSentiment(text);
        }
        double avgRaw = articles.isEmpty() ? 0 : totalRawScore / articles.size();

        result.setSentimentScore(Math.round(avgRaw * 100.0) / 100.0);
        result.setSentimentNormalized(sentimentService.normalizeSentimentScore(avgRaw));
        result.setSentiment(sentimentService.classifySentiment(avgRaw));
        log.info("{}: sentiment={} raw={} normalised={}", symbol,
                result.getSentiment(), result.getSentimentScore(), result.getSentimentNormalized());

        // ── Step 9: Signal 5 — Sentiment signal ─────────────────────────────
        String sentimentSignal = sentimentService.getSentimentSignal(avgRaw);

        // ── Step 10: Signal Fusion ───────────────────────────────────────────
        Map<String, String> signalVotes = new LinkedHashMap<>();
        signalVotes.put("SMA",       smaSignal);
        signalVotes.put("Crossover", crossoverSignal);
        signalVotes.put("Momentum",  momentumSignal);
        signalVotes.put("Volume",    volumeSignal);
        signalVotes.put("Sentiment", sentimentSignal);
        result.setSignalVotes(signalVotes);

        // ── Step 11: Tally votes ─────────────────────────────────────────────
        int bullish = 0, bearish = 0, neutral = 0;
        for (String vote : signalVotes.values()) {
            switch (vote.toLowerCase()) {
                case "bullish" -> bullish++;
                case "bearish" -> bearish++;
                default        -> neutral++;
            }
        }

        int total = signalVotes.size();
        String trend;
        double confidence;

        if (bullish > bearish) {
            trend      = "BULLISH";
            confidence = (bullish * 100.0) / total;
        } else if (bearish > bullish) {
            trend      = "BEARISH";
            confidence = (bearish * 100.0) / total;
        } else {
            trend      = "NEUTRAL";
            confidence = (neutral * 100.0) / total;
        }
        confidence = Math.min(confidence, 100.0);

        result.setTrend(trend);
        result.setConfidence(Math.round(confidence * 10.0) / 10.0);
        log.info("{}: trend={} confidence={}% (bull={} bear={} neutral={})",
                symbol, trend, result.getConfidence(), bullish, bearish, neutral);

        result.setSummary(buildSummary(result));
        return result;
    }

    /**
     * Volume signal derived from actual daily volume data.
     * Compares the most recent day's volume against the dataset average.
     * Returns neutral when insufficient data is available.
     */
    private String getVolumeSignal(StockData stockData) {
        List<Long> volumes = stockData.getDailyVolumes();
        if (volumes == null || volumes.size() < 5) return "neutral";

        double avg = volumes.stream().mapToLong(Long::longValue).average().orElse(0);
        if (avg == 0) return "neutral";

        long latest = volumes.get(volumes.size() - 1);
        if (latest > avg * 1.1) return "bullish";
        if (latest < avg * 0.9) return "bearish";
        return "neutral";
    }

    private String buildSummary(TrendResult r) {
        int smaPeriodDisplay = r.getEffectiveSmaPeriod() != null ? r.getEffectiveSmaPeriod() : shortPeriod;
        return String.format(
            "%s is showing a %s trend with %.0f%% confidence. " +
            "Price %.2f vs SMA(%d) %.2f | SMA(%d) %.2f. " +
            "Sentiment is %s (score: %.2f / normalised: %.2f).",
            r.getSymbol(), r.getTrend(), r.getConfidence(),
            r.getCurrentPrice(),
            smaPeriodDisplay, r.getSma20(),
            smaService.getLongPeriod(), r.getSma50(),
            r.getSentiment(), r.getSentimentScore(), r.getSentimentNormalized()
        );
    }
}
