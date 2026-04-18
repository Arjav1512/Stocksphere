# StockSphere v2.0 - Complete Project Context

## Executive Summary

**StockSphere** is an AI-powered stock trend analysis system that combines multiple technical analysis signals with sentiment analysis to forecast stock market trends. Built with **Spring Boot 3.2** and **Java 17**, it provides a REST API backend with a responsive dark-themed web frontend for real-time stock analysis and backtesting capabilities.

---

## 1. Project Type & Tech Stack

### Project Type
- **Backend**: REST API Web Service (Spring Boot)
- **Frontend**: Single-Page Application (Vanilla JavaScript)
- **Database**: None (in-memory data)
- **Build System**: Maven

### Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Framework | Spring Boot | 3.2.0 |
| Language | Java | 17 |
| Build Tool | Maven | 3.x |
| JSON Processing | Jackson DataBind | (via Spring Boot) |
| Testing | JUnit 5 (Jupiter) | (via Spring Boot) |
| Frontend | Vanilla JavaScript (ES6+) | - |
| Charts | Chart.js | 4.4.0 |
| Fonts | Google Fonts | Inter, Space Mono |
| Project Version | 2.0.0 | - |

---

## 2. Complete Directory & File Structure

```
stocksphere-upgraded/
├── pom.xml                                          # Maven configuration
├── .DS_Store                                        # macOS metadata
└── src/
    ├── main/
    │   ├── java/com/stocksphere/
    │   │   ├── StockSphereApplication.java          # Entry point (Spring Boot app)
    │   │   ├── config/
    │   │   │   └── WebConfig.java                   # CORS configuration
    │   │   ├── controller/
    │   │   │   └── StockController.java             # REST API endpoints
    │   │   ├── model/
    │   │   │   ├── StockData.java                   # Stock price data model
    │   │   │   ├── TrendResult.java                 # Analysis result model
    │   │   │   └── NewsArticle.java                 # News article model
    │   │   ├── dto/
    │   │   │   ├── StockResponse.java               # Stock endpoint DTO
    │   │   │   └── BacktestResponse.java            # Backtest endpoint DTO
    │   │   ├── service/
    │   │   │   ├── StockService.java                # Stock data retrieval
    │   │   │   ├── SMAService.java                  # SMA calculations & signals
    │   │   │   ├── TrendService.java                # Full analysis orchestration
    │   │   │   ├── SentimentService.java            # Sentiment analysis
    │   │   │   ├── NewsService.java                 # News retrieval (mock)
    │   │   │   └── BacktestService.java             # Backtesting logic
    │   │   ├── repository/
    │   │   │   └── DataLoader.java                  # Mock data generation
    │   │   └── exception/
    │   │       └── GlobalExceptionHandler.java      # Centralized error handling
    │   └── resources/
    │       ├── application.properties               # Configuration file
    │       ├── sentiment-dictionary.txt             # Sentiment lexicon
    │       └── static/
    │           ├── index.html                       # Main HTML page
    │           ├── script.js                        # Frontend logic
    │           └── styles.css                       # UI styling
    └── test/
        └── java/com/stocksphere/service/
            ├── SentimentServiceTest.java            # Sentiment tests
            ├── SMAServiceTest.java                  # SMA/momentum/crossover tests
            └── TrendServiceTest.java                # Full analysis tests
```

---

## 3. All Dependencies

### Maven Dependencies (pom.xml)

| Dependency | ArtifactId | Scope |
|-----------|-----------|-------|
| Spring Boot Starter Web | spring-boot-starter-web | compile |
| Jackson Databind | jackson-databind | compile (via Spring Boot) |
| Spring Boot Starter Test | spring-boot-starter-test | test |

### Maven Plugins

| Plugin | ArtifactId |
|--------|-----------|
| Spring Boot Maven Plugin | spring-boot-maven-plugin |

### Frontend CDN Dependencies

| Library | Version | Usage |
|---------|---------|-------|
| Chart.js | 4.4.0 | Price chart visualization |
| Google Fonts (Inter) | - | Sans-serif typography |
| Google Fonts (Space Mono) | - | Monospace typography |

---

## 4. Configuration Files

### `pom.xml`
- Java version: 17
- Project metadata: group `com.stocksphere`, artifact `stocksphere`, version `2.0.0`
- Spring Boot parent: 3.2.0
- Dependencies: Spring Web, Jackson, Spring Test
- Build plugins: Spring Boot Maven Plugin

### `application.properties`
Location: `src/main/resources/application.properties`

| Property | Default | Purpose |
|----------|---------|---------|
| `server.port` | 8080 | Server port |
| `spring.application.name` | StockSphere | Application name |
| `cors.allowed-origins` | * | CORS allowed origins |
| `app.use-mock-data` | true | Use mock data vs. live APIs |
| `alphavantage.api.key` | demo | Alpha Vantage API key (placeholder) |
| `newsapi.api.key` | demo | NewsAPI key (placeholder) |
| `stocksphere.sma.short` | 20 | Short-term SMA period |
| `stocksphere.sma.long` | 50 | Long-term SMA period |
| `logging.level.com.stocksphere` | INFO | Logging level |
| `logging.pattern.console` | `%d{HH:mm:ss} [%-5level] %logger{36} - %msg%n` | Log format |

### `sentiment-dictionary.txt`
Location: `src/main/resources/sentiment-dictionary.txt`

- Format: `word:score` (one per line)
- Score range: -3 to +3
  - Strongly positive +3: breakthrough, soar, skyrocket
  - Positive +2: profit, growth, strong, record, win
  - Weakly positive +1: gain, rise, demand
  - Strongly negative -3: recession, crisis, crash, collapse, selloff
  - Negative -2: loss, decline, drop, weak, miss, downgrade
  - Weakly negative -1: concern, risk, challenge
- Total entries: ~165 words (balanced positive/negative)

---

## 5. Source Files - Complete Reference

### Core Application Entry Point

#### `StockSphereApplication.java`
- Spring Boot application entry point
- Exports: `main(String[] args)` — application startup
- Annotation: `@SpringBootApplication`

---

### Configuration

#### `WebConfig.java`
- CORS configuration for cross-origin requests
- Exports: `corsConfigurer()` bean (WebMvcConfigurer)
- Annotation: `@Configuration`, `@Bean`
- Allows all origins (`*`), methods: GET, POST, PUT, DELETE, OPTIONS, all headers

---

### REST Controller

#### `StockController.java`
- REST API endpoint handler
- Exports:
  - `health()`: GET /health → service status
  - `getStock(symbol)`: GET /stock?symbol=X → StockResponse
  - `getNews(symbol)`: GET /news?symbol=X → List<NewsArticle>
  - `analyze(symbol, smaPeriod)`: GET /analyze?symbol=X → TrendResult
  - `backtest(symbol)`: GET /backtest?symbol=X → BacktestResponse
  - `validateSymbol(symbol)`: private validation helper
- Annotations: `@RestController`, `@CrossOrigin(origins = "*")`, `@GetMapping`, `@RequestParam`
- Validation:
  - Symbol: 1-12 chars, alphanumeric + dot only
  - smaPeriod (optional): range 2-200
  - Throws `IllegalArgumentException` caught by GlobalExceptionHandler
- Logging: SLF4J at INFO level

---

### Models

#### `StockData.java`
Internal model for stock price data.

Fields:
- `symbol`: String
- `closingPrices`: List<Double>
- `dates`: List<String>
- `currentPrice`: Double (auto-set from last price)
- `volume`: Long

#### `TrendResult.java`
Complete analysis result (returned by /analyze).

Fields:
- Price data: `currentPrice`, `prices`, `dates`
- SMA values: `sma20`, `sma50`, `sma` (backward compat alias)
- Sentiment: `sentimentScore` (raw), `sentimentNormalized` ([-1,+1]), `sentiment` (classification)
- Signals: `smaSignal`, `crossoverSignal`, `momentumSignal`, `volumeSignal`
- Fusion: `trend` (BULLISH/BEARISH/NEUTRAL), `confidence` (0-100), `signalVotes` (Map)
- News: `articles` (List<NewsArticle>)
- Summary: human-readable analysis text

#### `NewsArticle.java`
News article model.

Fields:
- `title`, `description`, `url`, `publishedAt`, `source`

---

### DTOs (Data Transfer Objects)

#### `StockResponse.java`
Typed DTO for /stock endpoint.

Fields: `symbol`, `prices` (List<Double>), `dates` (List<String>), `currentPrice` (Double), `volume` (Long)

#### `BacktestResponse.java`
DTO for /backtest endpoint.

Fields: `symbol`, `totalPredictions` (int), `correctPredictions` (int), `accuracyPercentage` (double), `strategy` (String), `note` (String)

---

### Services

#### `StockService.java`
- Stock data retrieval orchestration
- Exports: `getStockData(symbol)` → StockData
- Annotation: `@Service`
- Injects: `@Autowired DataLoader`, `@Value("${app.use-mock-data:true}")`
- Uses mock data by default; returns 60 days of price history

#### `SMAService.java`
- SMA calculations and trading signals
- Exports:
  - `calculateSMA(prices, period)`: Double — SMA on last N prices
  - `getSMASignal(currentPrice, sma)`: String — "bullish"/"bearish"/"neutral" (±0.5% threshold)
  - `getMomentumSignal(prices)`: String — today vs yesterday price comparison
  - `getCrossoverSignal(prices)`: String — SMA20 vs SMA50 crossover
  - `getShortPeriod()`, `getLongPeriod()`: int — configured periods
- Annotation: `@Service`
- Injects: `@Value("${stocksphere.sma.short:20}")`, `@Value("${stocksphere.sma.long:50}")`

#### `TrendService.java`
- Full multi-signal trend analysis orchestration
- Exports: `analyze(symbol)` → TrendResult
- Annotation: `@Service`
- Injects: SMAService, SentimentService, NewsService, StockService
- Analysis steps:
  1. Fetch stock data
  2. Calculate SMA(20) and SMA(50)
  3. Get SMA signal (price vs SMA20)
  4. Get momentum signal (today vs yesterday)
  5. Get crossover signal (SMA20 vs SMA50)
  6. Get volume signal (deterministic hash-based)
  7. Fetch news articles
  8. Calculate sentiment (raw + normalized [-1,+1])
  9. Get sentiment signal
  10. Fuse 5 signals by vote → trend + confidence
  11. Build summary text

#### `SentimentService.java`
- Lexicon-based sentiment analysis
- Exports:
  - `analyzeSentiment(text)`: double — raw score
  - `normalizeSentimentScore(rawScore)`: double — normalized to [-1,+1]
  - `classifySentiment(score)`: String — "positive"/"negative"/"neutral"
  - `getSentimentSignal(score)`: String — "bullish"/"bearish"/"neutral"
- Annotation: `@Service`, `@PostConstruct` (dictionary loading)
- Dictionary loaded from `sentiment-dictionary.txt` on startup; falls back to built-in dictionary
- Analysis process: lowercase + remove non-alpha (keep spaces) → split into words → lookup → sum scores
- Normalization: rawScore / 10.0, clamped to [-1,+1]

#### `NewsService.java`
- News article retrieval (mock implementation)
- Exports: `getNewsArticles(symbol)` → List<NewsArticle> (6 articles per symbol)
- Annotation: `@Service`
- Symbol-specific templates for: RELIANCE, TCS, AAPL, TSLA; generic for others
- Dates: going back 6 hours per article from now; generation is deterministic

Sample news per symbol:
- **RELIANCE**: earnings, Jio, retail, analyst upgrades, green energy, margin pressure
- **TCS**: UK deal, revenue growth, AI/cloud, hiring slowdown, deal pipeline, dividend
- **AAPL**: iPhone sales, Vision Pro, services, India manufacturing, AI, EU regulation
- **TSLA**: Cybertruck, price cuts, Supercharger, energy storage, governance, FSD

#### `BacktestService.java`
- Walk-forward backtesting of SMA crossover + momentum strategy
- Exports: `runBacktest(symbol)` → BacktestResponse
- Annotation: `@Service`
- Injects: StockService, SMAService, `@Value` for SMA periods
- Algorithm:
  - Requires minimum: longPeriod + 2 prices (52 days for SMA50)
  - Walk forward: for each day from index longPeriod to prices.size()-1
  - Predict direction via SMA20/50 crossover + momentum fusion
  - Compare prediction vs actual next-day move (up/down)
  - Skip "neutral" uncertain predictions
  - Output: (correct / total) * 100, rounded to 0.1%

---

### Repository

#### `DataLoader.java`
- Mock stock data generation
- Exports: `loadMockStockData(symbol)` → StockData (60 trading days)
- Annotation: `@Component`
- 60 trading days (excludes weekends); Gaussian random walk with drift
- Deterministic seed: `symbol.hashCode()`; volume: 500k-5.5M shares/day

Supported symbols:
- Indian: RELIANCE, TCS, INFY, HDFCBANK, WIPRO, ICICIBANK, TATAMOTORS, BAJFINANCE, HCLTECH, SBIN
- US: AAPL, MSFT, GOOGL, AMZN, TSLA, NVDA, META, NFLX

---

### Exception Handling

#### `GlobalExceptionHandler.java`
- Centralized exception handling for REST API
- Annotation: `@RestControllerAdvice`, `@ExceptionHandler`
- Exports:
  - `handleIllegalArgument(...)` → 400 VALIDATION_ERROR
  - `handleMissingParam(...)` → 400 MISSING_PARAMETER
  - `handleRuntime(...)` → 500 INTERNAL_ERROR
  - `handleGeneric(...)` → 500 SERVER_ERROR
- Response format:
  ```json
  {
    "timestamp": "2024-04-19T12:34:56Z",
    "status": 400,
    "error": "VALIDATION_ERROR",
    "message": "Stock symbol is required..."
  }
  ```

---

### Frontend

#### `script.js`
SPA logic for StockSphere web interface.

Key functions:
- `analyzeStock()`: Fetch /analyze, render results
- `runBacktest()`: Fetch /backtest, show accuracy
- `checkHealth()`: Verify backend connection (GET /health)
- `renderResults(data)`: Display trend analysis
- `renderChart(data)`: Draw price + SMA20/SMA50 chart (Chart.js)
- `renderSignals(data)`: Display signal fusion grid
- `renderSentiment(data)`: Show sentiment meter + news
- `renderBacktest(data)`: Display backtest accuracy
- `showError(symbol, message)`: Error state display
- Utilities: `fmtPrice()`, `formatDate()`, `escapeHtml()`

Global variables:
- `API_BASE = 'http://localhost:8080'`
- `priceChart`: Chart.js instance
- `lastSymbol`: For retry functionality

Event listeners:
- Enter key in search input → analyze
- Input change → uppercase symbol
- Quick chips → quick analyze

Chart config:
- Type: Line chart
- Data: Price (green + area), SMA20 (gold dash), SMA50 (purple dash)
- Responsive, interactive tooltips, Space Mono font

#### `index.html`
Structure:
- Header: logo, subtitle, status badge
- Search: input, analyze/backtest buttons, quick chips
- Loading overlay: animated spinner + steps
- Results panel: summary, metrics, chart, signals, sentiment, backtest
- Empty state (initial), error state (failed)

Sections:
- Summary banner: symbol + trend pill
- Metrics grid (4 cols): Current price, SMA(20)/(50), Sentiment score, Confidence
- Chart: interactive price + SMA history
- Signal fusion grid: 5 signal cards + confidence bar
- Sentiment: normalized meter [-1,+1] + news list
- Backtest panel: predictions count + accuracy

#### `styles.css`
Theme: Dark financial terminal aesthetic.

Color scheme:
- Backgrounds: #090c10 (main), #0e1318 (secondary), #141a22 (tertiary)
- Text: #d4dde8 (primary), #5a7085 (dim), #3a5060 (muted)
- Accent: #00c896 (bullish green), #f04a5e (bearish red), #f5c842 (gold), #a78bfa (purple)

Layout: CSS Grid (metrics), Flexbox (header, signals), sticky header, max-width 1200px, 768px breakpoint

Animations:
- `pulse-dot`: Logo pulsing
- `spin`: Loading spinner
- `fadeUp`: Results fade-in
- `float`: Empty state icon
- Confidence bar: 1s cubic-bezier fill
- Sentiment needle: 0.8s transition

---

## 6. Test Files

All tests use JUnit 5 (Jupiter) with Spring Test utilities.

### `SentimentServiceTest.java` — 30 test methods
- Basic polarity: positive, negative, neutral
- Text normalization: punctuation stripping, case insensitivity
- Classification: positive/negative/neutral classes
- Sentiment signals: bullish/bearish from raw scores
- Normalization: score clamping to [-1,+1]
- Edge cases: null, empty, extreme scores

### `SMAServiceTest.java` — 20 test methods
- Basic SMA: calculation on last N prices
- SMA signals: bullish/bearish/neutral
- Momentum: today vs yesterday
- Crossover: SMA20 vs SMA50
- Error handling: null, zero, negative periods

### `TrendServiceTest.java` — 15 test methods
- Full analysis: symbol, prices, SMA values, trend, confidence
- Signal voting: 5 signals (SMA, Crossover, Momentum, Volume, Sentiment)
- Sentiment normalization: score in [-1,+1]
- Classification: sentiment and trend validity
- News articles: minimum 5+ articles loaded

---

## 7. Database Schema

No persistent database is used. All data is in-memory:

- **Stock data**: 60 days of closing prices + dates per symbol (ephemeral)
- **Sentiment dictionary**: ~165 word → score entries loaded into `Map<String, Integer>`
- **News**: template-based, generated per request

---

## 8. API Endpoints — Complete Reference

Base URL: `http://localhost:8080`

### GET /health
Returns service status.

Response `200 OK`:
```json
{ "status": "running", "service": "StockSphere", "version": "2.0.0" }
```

### GET /stock?symbol={symbol}
Retrieve historical price data.

Parameters: `symbol` (required, 1-12 chars, alphanumeric + dot)

Response `200 OK`:
```json
{
  "symbol": "RELIANCE",
  "prices": [2450.0, 2455.0, "..."],
  "dates": ["2024-02-19", "2024-02-20", "..."],
  "currentPrice": 2600.5,
  "volume": 3500000
}
```

Errors: `400` if symbol blank, > 12 chars, or invalid chars.

### GET /news?symbol={symbol}
Fetch recent news.

Response `200 OK`: array of NewsArticle objects (6 articles):
```json
[
  {
    "title": "...",
    "description": "...",
    "url": "https://finance.example.com/news/reliance-0",
    "publishedAt": "2024-04-19 12:34",
    "source": "Economic Times"
  }
]
```

### GET /analyze?symbol={symbol}[&smaPeriod={period}]
Run full trend analysis.

Parameters:
- `symbol` (required)
- `smaPeriod` (optional, default 20, range 2-200)

Response `200 OK`:
```json
{
  "symbol": "RELIANCE",
  "currentPrice": 2600.5,
  "sma20": 2550.0,
  "sma50": 2520.0,
  "sma": 2550.0,
  "sentimentScore": 5.0,
  "sentimentNormalized": 0.5,
  "sentiment": "positive",
  "smaSignal": "bullish",
  "crossoverSignal": "bullish",
  "momentumSignal": "bullish",
  "volumeSignal": "bearish",
  "trend": "BULLISH",
  "confidence": 80.0,
  "signalVotes": {
    "SMA": "bullish",
    "Crossover": "bullish",
    "Momentum": "bullish",
    "Volume": "bearish",
    "Sentiment": "bullish"
  },
  "prices": ["..."],
  "dates": ["..."],
  "articles": ["..."],
  "summary": "RELIANCE is showing a BULLISH trend with 80% confidence..."
}
```

Errors: `400` if symbol invalid, or smaPeriod < 2 or > 200.

### GET /backtest?symbol={symbol}
Walk-forward backtest of SMA20/SMA50 + momentum strategy.

Response `200 OK`:
```json
{
  "symbol": "RELIANCE",
  "totalPredictions": 8,
  "correctPredictions": 5,
  "accuracyPercentage": 62.5,
  "strategy": "SMA Crossover + Momentum",
  "note": "Walk-forward backtest over 60 historical trading days"
}
```

Note: Returns totalPredictions=0 if insufficient data (< 52 days).

---

## 9. Authentication & Authorization

None implemented. All endpoints are public with CORS open to all origins (`*`). No API keys, sessions, roles, or permissions required.

---

## 10. Environment Variables

No environment variables are used. All configuration is in `application.properties`. Live API keys (`alphavantage.api.key`, `newsapi.api.key`) are placeholder `"demo"` values.

---

## 11. Available Scripts

### Maven Commands

| Command | Purpose |
|---------|---------|
| `mvn clean install` | Clean, compile, test, package |
| `mvn clean` | Remove target/ directory |
| `mvn compile` | Compile source code |
| `mvn test` | Run all tests (JUnit 5) |
| `mvn test -Dtest=SentimentServiceTest` | Run specific test class |
| `mvn spring-boot:run` | Run application |
| `mvn package` | Create JAR file |
| `java -jar target/stocksphere-2.0.0.jar` | Run packaged JAR |

### Development Workflow
```bash
# Start backend
mvn spring-boot:run

# Access frontend
# http://localhost:8080/

# Test API
curl http://localhost:8080/health
curl http://localhost:8080/analyze?symbol=RELIANCE

# Run tests
mvn test
```

---

## 12. External Services / APIs

### Configured but Not Implemented

| Service | Config Key | Default | Purpose | Status |
|---------|-----------|---------|---------|--------|
| Alpha Vantage | `alphavantage.api.key` | "demo" | Real stock prices | Placeholder (mock used) |
| NewsAPI | `newsapi.api.key` | "demo" | Real news articles | Placeholder (mock used) |

All data is mock-generated deterministically. The code has comments indicating where real API calls would go:
```java
// Production: call Alpha Vantage / Yahoo Finance here
return dataLoader.loadMockStockData(symbol);
```

---

## 13. State Management

### Backend
- Stateless: each request is independent
- Spring `@Service` beans are singletons (thread-safe)
- No sessions; no persistent state between requests

### Frontend
- Global variables: `API_BASE`, `priceChart` (Chart.js instance), `lastSymbol` (retry)
- State implicit in DOM (input value, visibility)
- No state management framework; direct DOM manipulation

---

## 14. Architecture & Design Patterns

### Layered Architecture
```
Controllers (HTTP layer)
    ↓
Services (Business logic)
    ↓
Repository (Data access)
```

### Service Responsibilities
- `StockService`: Data retrieval
- `SMAService`: Technical analysis (SMA, momentum, crossover)
- `SentimentService`: NLP sentiment scoring
- `NewsService`: News aggregation
- `TrendService`: Orchestration of all signals
- `BacktestService`: Strategy evaluation

### Signal Fusion Pattern
5 signals voted by majority:
```java
signalVotes.put("SMA", smaSignal);
signalVotes.put("Crossover", crossoverSignal);
signalVotes.put("Momentum", momentumSignal);
signalVotes.put("Volume", volumeSignal);
signalVotes.put("Sentiment", sentimentSignal);
// Vote tally → BULLISH/BEARISH/NEUTRAL + confidence %
```

### Centralized Exception Handling
`@RestControllerAdvice` maps exceptions to consistent JSON error responses with timestamp, status, error code, and message.

### Configuration Externalization
SMA periods, logging, API keys, and server port all in `application.properties` injected via `@Value`.

### Lexicon-Based Sentiment
Dictionary approach (no ML library), additive scoring model, normalized to [-1,+1].

### Walk-Forward Backtesting
Simulates real-world conditions, avoids lookahead bias, tests SMA20/50 crossover + momentum combo.

### Deterministic Mock Data
Symbol-based seeding (`symbol.hashCode()`), Gaussian random walk with fixed volatility — repeatable results for testing.

---

## 15. Feature Completeness (Task Tracking)

| Task | Feature | Status | Location |
|------|---------|--------|----------|
| 1 | Typed DTO responses | Done | StockResponse, BacktestResponse, TrendResult |
| 2 | Input validation + 400 errors | Done | StockController.validateSymbol() |
| 3 | Centralized exception handling | Done | GlobalExceptionHandler |
| 4 | Text normalization | Done | SentimentService.analyzeSentiment() |
| 5 | SMA calculation (last N prices) | Done | SMAService.calculateSMA() |
| 6 | Momentum signal (today vs yesterday) | Done | SMAService.getMomentumSignal() |
| 7 | SMA20/SMA50 crossover | Done | SMAService.getCrossoverSignal() |
| 8 | Normalized sentiment [-1,+1] | Done | SentimentService.normalizeSentimentScore() |
| 9 | Volume signal | Done | TrendService (hash-based) |
| 10 | 5-signal fusion | Done | TrendService.analyze() |
| 11 | Web UI | Done | index.html, script.js, styles.css |
| 12 | SLF4J logging | Done | All services, controller, handler |
| 13 | Configurable SMA periods | Done | application.properties + @Value injection |
| 14 | Backtesting feature | Done | BacktestService.runBacktest() |

---

## 16. Project Statistics

- Java files: 13 (5 models/DTOs, 6 services, 1 controller, 1 config, 1 exception handler, 1 entry point)
- Test files: 3 (65 total test methods)
- Frontend files: 3 (HTML, JS, CSS)
- Configuration files: 2 (pom.xml, application.properties)
- Resource files: 1 (sentiment-dictionary.txt)
- Estimated lines of code: ~2,500 backend + ~1,000 frontend = ~3,500 total
- Dependencies: 3 compile + 1 test
- Test coverage: service layer comprehensive; controller and integration not tested

---

## 17. Known Limitations

1. No persistent storage — data lost on restart
2. Mock data only — no live API integration (placeholders exist)
3. Synchronous processing — no async/reactive support
4. Volume signal simplistic — hash-based, not real volume analysis
5. No caching — regenerates data on every request
6. No pagination — news limited to 6 articles
7. Limited symbol coverage — 18 pre-configured symbols
8. No authentication — all endpoints public
9. Fixed SMA periods at config level — smaPeriod query param not wired through to SMAService

---

## 18. Running & Deploying

### Local Development
```bash
# Prerequisites: Java 17+, Maven 3.8+

cd /Users/arjavjain/Downloads/Projects/stocksphere-upgraded

# Run backend
mvn spring-boot:run

# Open browser at http://localhost:8080/
```

### Building a JAR
```bash
mvn clean package
java -jar target/stocksphere-2.0.0.jar
```

### Switching to Live APIs
Update `application.properties`:
```properties
app.use-mock-data=false
alphavantage.api.key=YOUR_KEY
newsapi.api.key=YOUR_KEY
logging.level.com.stocksphere=WARN
```
