package com.stocksphere.repository;

import com.stocksphere.model.StockData;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class DataLoader {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public StockData loadMockStockData(String symbol) {
        Map<String, Double[]> baseData = getMockBaseData();
        String upperSymbol = symbol.toUpperCase();

        double[] seed = getOrDefault(baseData, upperSymbol);
        double basePrice  = seed[0];
        double volatility = seed[1];

        List<Double> prices     = new ArrayList<>();
        List<String> dates      = new ArrayList<>();
        List<Long>   dailyVols  = new ArrayList<>();

        Random rand = new Random(upperSymbol.hashCode());
        double price = basePrice;
        LocalDate date = LocalDate.now().minusDays(60);

        for (int i = 0; i < 60; i++) {
            while (date.getDayOfWeek().getValue() > 5) {
                date = date.plusDays(1);
            }
            double change = (rand.nextGaussian() * volatility) + (basePrice * 0.001);
            price = Math.max(price + change, basePrice * 0.5);
            prices.add(Math.round(price * 100.0) / 100.0);
            dates.add(date.format(DATE_FMT));
            dailyVols.add(500_000L + (long)(rand.nextDouble() * 5_000_000));
            date = date.plusDays(1);
        }

        StockData stockData = new StockData(upperSymbol, prices, dates);
        stockData.setDailyVolumes(dailyVols);
        stockData.setVolume(dailyVols.get(dailyVols.size() - 1));
        return stockData;
    }

    private double[] getOrDefault(Map<String, Double[]> map, String symbol) {
        Double[] val = map.get(symbol);
        if (val == null) return new double[]{1000.0, 10.0};
        return new double[]{val[0], val[1]};
    }

    private Map<String, Double[]> getMockBaseData() {
        Map<String, Double[]> data = new LinkedHashMap<>();
        data.put("RELIANCE",   new Double[]{2500.0, 25.0});
        data.put("TCS",        new Double[]{3800.0, 30.0});
        data.put("INFY",       new Double[]{1500.0, 15.0});
        data.put("HDFCBANK",   new Double[]{1650.0, 20.0});
        data.put("WIPRO",      new Double[]{450.0,   8.0});
        data.put("ICICIBANK",  new Double[]{950.0,  12.0});
        data.put("TATAMOTORS", new Double[]{820.0,  18.0});
        data.put("BAJFINANCE", new Double[]{7000.0, 80.0});
        data.put("HCLTECH",    new Double[]{1200.0, 14.0});
        data.put("SBIN",       new Double[]{620.0,  10.0});
        data.put("AAPL",  new Double[]{185.0,  3.0});
        data.put("MSFT",  new Double[]{410.0,  6.0});
        data.put("GOOGL", new Double[]{165.0,  3.5});
        data.put("AMZN",  new Double[]{195.0,  4.0});
        data.put("TSLA",  new Double[]{200.0, 10.0});
        data.put("NVDA",  new Double[]{875.0, 20.0});
        data.put("META",  new Double[]{500.0, 12.0});
        data.put("NFLX",  new Double[]{620.0, 15.0});
        return data;
    }
}
