# StockSphere

StockSphere is a stock trend analysis tool that combines technical signals with news sentiment to give you a clear picture of where a stock might be heading. It's not a trading bot, and it doesn't claim to predict the market — but it pulls together five different signals (SMA crossover, momentum, volume, and sentiment) and fuses them into a single verdict: **BULLISH**, **BEARISH**, or **NEUTRAL**, with a confidence score to back it up.

Built with Spring Boot on the backend and plain vanilla JS on the frontend — no React, no npm, no build step. Just run it and open your browser.

---

## What it actually does

You type in a stock symbol (say, `RELIANCE` or `TSLA`), hit analyze, and it shows you:

- A price chart with SMA20 and SMA50 overlaid
- Five individual signal cards (SMA, Crossover, Momentum, Volume, Sentiment)
- A sentiment score pulled from recent news headlines
- A backtesting result — how often the SMA+momentum strategy would've been right over the last 60 trading days

Right now everything runs on mock data. The data is seeded from the symbol name, so it's deterministic — the same symbol always gives the same chart. Real API integrations (Alpha Vantage, NewsAPI) are stubbed out and ready to wire up; you just need the keys.

---

## Tech stack

| Layer     | What's used              |
|-----------|--------------------------|
| Backend   | Java 17, Spring Boot 3.2 |
| Build     | Maven                    |
| Frontend  | Vanilla JS, Chart.js 4.4 |
| Tests     | JUnit 5 (65 test methods) |
| Database  | None (in-memory)         |

---

## Getting started

**Prerequisites:** Java 17+ and Maven 3.8+. That's it.

```bash
git clone https://github.com/Arjav1512/Stocksphere.git
cd Stocksphere
mvn spring-boot:run
```

Then open [http://localhost:8080](http://localhost:8080) in your browser.

---

## Supported symbols

The mock data generator has 18 pre-configured symbols:

**Indian:** `RELIANCE`, `TCS`, `INFY`, `HDFCBANK`, `WIPRO`, `ICICIBANK`, `TATAMOTORS`, `BAJFINANCE`, `HCLTECH`, `SBIN`

**US:** `AAPL`, `MSFT`, `GOOGL`, `AMZN`, `TSLA`, `NVDA`, `META`, `NFLX`

Any other symbol will still work — it'll just get generic mock data.

---

## API reference

All endpoints are under `http://localhost:8080`.

| Endpoint | What it returns |
|----------|-----------------|
| `GET /health` | Service status + version |
| `GET /stock?symbol=X` | 60 days of closing prices |
| `GET /news?symbol=X` | 6 recent news articles |
| `GET /analyze?symbol=X` | Full trend analysis with all 5 signals |
| `GET /backtest?symbol=X` | Walk-forward backtest accuracy over 60 days |

The `/analyze` endpoint also accepts an optional `smaPeriod` param (default 20, range 2–200) if you want to tune the short-term SMA window.

**Example:**
```bash
curl http://localhost:8080/analyze?symbol=RELIANCE
```

---

## How the analysis works

The trend engine runs five signals and takes a majority vote:

1. **SMA Signal** — is the current price above or below the 20-day moving average?
2. **Crossover Signal** — has SMA20 crossed above/below SMA50?
3. **Momentum Signal** — did the price go up or down today vs yesterday?
4. **Volume Signal** — deterministic hash-based (placeholder for real volume data)
5. **Sentiment Signal** — lexicon-based score from news headlines (~165 word dictionary)

Whichever direction gets the most votes wins. Confidence scales with how many signals agree (e.g. 5/5 = 100%, 3/5 = ~60%).

---

## Running tests

```bash
mvn test
```

There are 65 tests across three files covering the sentiment analyzer, SMA calculations, and the full trend service.

To run a specific test class:
```bash
mvn test -Dtest=SentimentServiceTest
```

---

## Switching to live data

When you're ready to plug in real APIs, update `src/main/resources/application.properties`:

```properties
app.use-mock-data=false
alphavantage.api.key=YOUR_KEY_HERE
newsapi.api.key=YOUR_KEY_HERE
```

The hooks for Alpha Vantage (stock prices) and NewsAPI (headlines) are already in the code — just need your keys and the mock flag flipped off.

---

## Building a JAR

```bash
mvn clean package
java -jar target/stocksphere-2.0.0.jar
```

---

## Known limitations

A few things worth knowing before you dig in:

- All data is in-memory — nothing persists across restarts
- Volume signal is hash-based, not real volume analysis
- No authentication on any endpoint
- News limited to 6 articles per symbol, not paginated
- The `smaPeriod` query param on `/analyze` isn't wired through to the SMA service yet (tracked as a known gap)
- 18 supported symbols; everything else falls back to a generic mock

---

## Project structure

```
src/
├── main/java/com/stocksphere/
│   ├── controller/      # REST endpoints
│   ├── service/         # Business logic (SMA, sentiment, trend, backtest)
│   ├── model/           # Data models
│   ├── dto/             # Response DTOs
│   ├── repository/      # Mock data generation
│   └── exception/       # Global error handling
└── main/resources/
    ├── application.properties
    ├── sentiment-dictionary.txt
    └── static/          # Frontend (HTML, JS, CSS)
```

---

## License

No license file included. Reach out to [Arjav Jain](https://github.com/Arjav1512) if you want to use this for something.
