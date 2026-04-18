package com.stocksphere.dto;

import java.util.List;

/**
 * Typed DTO for /stock endpoint responses (Task 1 — no raw Maps in controllers).
 */
public class StockResponse {
    private String symbol;
    private List<Double> prices;
    private List<String> dates;
    private Double currentPrice;
    private Long volume;

    public StockResponse() {}

    public StockResponse(String symbol, List<Double> prices, List<String> dates,
                         Double currentPrice, Long volume) {
        this.symbol = symbol;
        this.prices = prices;
        this.dates = dates;
        this.currentPrice = currentPrice;
        this.volume = volume;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public List<Double> getPrices() { return prices; }
    public void setPrices(List<Double> prices) { this.prices = prices; }
    public List<String> getDates() { return dates; }
    public void setDates(List<String> dates) { this.dates = dates; }
    public Double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }
    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }
}
