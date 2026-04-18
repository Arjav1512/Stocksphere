package com.stocksphere.dto;

/**
 * DTO for /backtest endpoint (Task 14 — Backtesting Feature).
 */
public class BacktestResponse {
    private String symbol;
    private int totalPredictions;
    private int correctPredictions;
    private double accuracyPercentage;
    private String strategy;
    private String note;

    public BacktestResponse() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public int getTotalPredictions() { return totalPredictions; }
    public void setTotalPredictions(int totalPredictions) { this.totalPredictions = totalPredictions; }
    public int getCorrectPredictions() { return correctPredictions; }
    public void setCorrectPredictions(int correctPredictions) { this.correctPredictions = correctPredictions; }
    public double getAccuracyPercentage() { return accuracyPercentage; }
    public void setAccuracyPercentage(double accuracyPercentage) { this.accuracyPercentage = accuracyPercentage; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
