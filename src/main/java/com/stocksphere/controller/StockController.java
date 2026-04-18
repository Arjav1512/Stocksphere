package com.stocksphere.controller;

import com.stocksphere.dto.BacktestResponse;
import com.stocksphere.dto.StockResponse;
import com.stocksphere.model.NewsArticle;
import com.stocksphere.model.StockData;
import com.stocksphere.model.TrendResult;
import com.stocksphere.service.BacktestService;
import com.stocksphere.service.NewsService;
import com.stocksphere.service.StockService;
import com.stocksphere.service.TrendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller — StockSphere API.
 *
 * Task 1:  Typed DTO responses (no raw Map<String,Object> in successful paths).
 * Task 2:  Input validation with meaningful 400 errors.
 * Task 12: SLF4J logging.
 *
 * CORS is configured globally in WebConfig — @CrossOrigin is intentionally absent here.
 * Error responses are handled centrally by GlobalExceptionHandler.
 */
@RestController
public class StockController {

    private static final Logger log = LoggerFactory.getLogger(StockController.class);

    @Autowired private TrendService    trendService;
    @Autowired private StockService    stockService;
    @Autowired private NewsService     newsService;
    @Autowired private BacktestService backtestService;

    // ── Health ─────────────────────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status",  "running",
                "service", "StockSphere",
                "version", "2.0.0"
        ));
    }

    // ── Stock Data ─────────────────────────────────────────────────────────────

    @GetMapping("/stock")
    public ResponseEntity<StockResponse> getStock(@RequestParam String symbol) {
        validateSymbol(symbol);
        log.info("GET /stock?symbol={}", symbol.toUpperCase());
        StockData data = stockService.getStockData(symbol);
        StockResponse response = new StockResponse(
                data.getSymbol(),
                data.getClosingPrices(),
                data.getDates(),
                data.getCurrentPrice(),
                data.getVolume()
        );
        return ResponseEntity.ok(response);
    }

    // ── News ───────────────────────────────────────────────────────────────────

    @GetMapping("/news")
    public ResponseEntity<List<NewsArticle>> getNews(@RequestParam String symbol) {
        validateSymbol(symbol);
        log.info("GET /news?symbol={}", symbol.toUpperCase());
        return ResponseEntity.ok(newsService.getNewsArticles(symbol));
    }

    // ── Analyze ────────────────────────────────────────────────────────────────

    /**
     * GET /analyze?symbol=RELIANCE[&smaPeriod=20]
     *
     * smaPeriod is validated here and passed through to TrendService so the
     * analysis honours the caller's requested period (was silently ignored before).
     */
    @GetMapping("/analyze")
    public ResponseEntity<TrendResult> analyze(
            @RequestParam String symbol,
            @RequestParam(required = false) Integer smaPeriod) {

        validateSymbol(symbol);
        if (smaPeriod != null && (smaPeriod < 2 || smaPeriod > 200)) {
            throw new IllegalArgumentException(
                    "smaPeriod must be between 2 and 200, got: " + smaPeriod);
        }

        log.info("GET /analyze?symbol={}&smaPeriod={}", symbol.toUpperCase(), smaPeriod);
        TrendResult result = trendService.analyze(symbol, smaPeriod);
        return ResponseEntity.ok(result);
    }

    // ── Backtest ───────────────────────────────────────────────────────────────

    @GetMapping("/backtest")
    public ResponseEntity<BacktestResponse> backtest(@RequestParam String symbol) {
        validateSymbol(symbol);
        log.info("GET /backtest?symbol={}", symbol.toUpperCase());
        return ResponseEntity.ok(backtestService.runBacktest(symbol));
    }

    // ── Validation helper ──────────────────────────────────────────────────────

    private void validateSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Stock symbol is required and cannot be blank");
        }
        String trimmed = symbol.trim();
        if (trimmed.length() < 1 || trimmed.length() > 12) {
            throw new IllegalArgumentException(
                    "Stock symbol must be between 1 and 12 characters, got: '" + trimmed + "'");
        }
        if (!trimmed.matches("[A-Za-z0-9.]+")) {
            throw new IllegalArgumentException(
                    "Stock symbol may only contain letters, digits and '.', got: '" + trimmed + "'");
        }
    }
}
