package com.stocksphere.model;

import java.util.List;
import java.util.Map;

/**
 * Full analysis result returned by /analyze endpoint.
 */
public class TrendResult {
    private String symbol;
    private Double currentPrice;

    // SMA scalar values
    private Double sma20;
    private Double sma50;
    /** Kept for backward compat — always equals sma20 */
    private Double sma;

    // Rolling SMA history arrays (one value per price date, null where insufficient history)
    private List<Double> sma20History;
    private List<Double> sma50History;

    // The SMA period actually used for this response (honoring per-request smaPeriod override)
    private Integer effectiveSmaPeriod;

    // Sentiment
    private Double sentimentScore;
    private Double sentimentNormalized;
    private String sentiment;

    // Individual signals
    private String smaSignal;
    private String crossoverSignal;
    private String momentumSignal;
    private String volumeSignal;

    // Fusion output
    private String trend;
    private Double confidence;
    private Map<String, String> signalVotes;

    // Raw data for chart
    private List<Double> prices;
    private List<String> dates;

    // News
    private List<NewsArticle> articles;

    private String summary;

    public TrendResult() {}

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }

    public Double getSma20() { return sma20; }
    public void setSma20(Double sma20) { this.sma20 = sma20; this.sma = sma20; }

    public Double getSma50() { return sma50; }
    public void setSma50(Double sma50) { this.sma50 = sma50; }

    public Double getSma() { return sma; }
    public void setSma(Double sma) { this.sma = sma; }

    public List<Double> getSma20History() { return sma20History; }
    public void setSma20History(List<Double> sma20History) { this.sma20History = sma20History; }

    public List<Double> getSma50History() { return sma50History; }
    public void setSma50History(List<Double> sma50History) { this.sma50History = sma50History; }

    public Integer getEffectiveSmaPeriod() { return effectiveSmaPeriod; }
    public void setEffectiveSmaPeriod(Integer effectiveSmaPeriod) { this.effectiveSmaPeriod = effectiveSmaPeriod; }

    public Double getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(Double sentimentScore) { this.sentimentScore = sentimentScore; }

    public Double getSentimentNormalized() { return sentimentNormalized; }
    public void setSentimentNormalized(Double sentimentNormalized) { this.sentimentNormalized = sentimentNormalized; }

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    public String getSmaSignal() { return smaSignal; }
    public void setSmaSignal(String smaSignal) { this.smaSignal = smaSignal; }

    public String getCrossoverSignal() { return crossoverSignal; }
    public void setCrossoverSignal(String crossoverSignal) { this.crossoverSignal = crossoverSignal; }

    public String getMomentumSignal() { return momentumSignal; }
    public void setMomentumSignal(String momentumSignal) { this.momentumSignal = momentumSignal; }

    public String getVolumeSignal() { return volumeSignal; }
    public void setVolumeSignal(String volumeSignal) { this.volumeSignal = volumeSignal; }

    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public Map<String, String> getSignalVotes() { return signalVotes; }
    public void setSignalVotes(Map<String, String> signalVotes) { this.signalVotes = signalVotes; }

    public List<Double> getPrices() { return prices; }
    public void setPrices(List<Double> prices) { this.prices = prices; }

    public List<String> getDates() { return dates; }
    public void setDates(List<String> dates) { this.dates = dates; }

    public List<NewsArticle> getArticles() { return articles; }
    public void setArticles(List<NewsArticle> articles) { this.articles = articles; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
