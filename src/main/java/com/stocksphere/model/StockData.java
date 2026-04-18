package com.stocksphere.model;

import java.util.List;

public class StockData {
    private String symbol;
    private List<Double> closingPrices;
    private List<String> dates;
    private Double currentPrice;
    private Long volume;
    private List<Long> dailyVolumes;

    public StockData() {}

    public StockData(String symbol, List<Double> closingPrices, List<String> dates) {
        this.symbol = symbol;
        this.closingPrices = closingPrices;
        this.dates = dates;
        if (closingPrices != null && !closingPrices.isEmpty()) {
            this.currentPrice = closingPrices.get(closingPrices.size() - 1);
        }
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public List<Double> getClosingPrices() { return closingPrices; }
    public void setClosingPrices(List<Double> closingPrices) {
        this.closingPrices = closingPrices;
        if (closingPrices != null && !closingPrices.isEmpty()) {
            this.currentPrice = closingPrices.get(closingPrices.size() - 1);
        }
    }

    public List<String> getDates() { return dates; }
    public void setDates(List<String> dates) { this.dates = dates; }

    public Double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }

    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }

    public List<Long> getDailyVolumes() { return dailyVolumes; }
    public void setDailyVolumes(List<Long> dailyVolumes) { this.dailyVolumes = dailyVolumes; }
}
