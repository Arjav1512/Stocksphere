package com.stocksphere.service;

import com.stocksphere.model.NewsArticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NewsService {

    private static final Logger log = LoggerFactory.getLogger(NewsService.class);
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public List<NewsArticle> getNewsArticles(String symbol) {
        String upperSymbol = symbol.toUpperCase();
        List<String[]> templates = getNewsTemplates(upperSymbol);
        List<NewsArticle> articles = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < templates.size(); i++) {
            String[] t = templates.get(i);
            String date = now.minusHours(i * 6L).format(DT_FMT);
            articles.add(new NewsArticle(
                    t[0].replace("{{SYMBOL}}", upperSymbol),
                    t[1].replace("{{SYMBOL}}", upperSymbol),
                    "https://finance.example.com/news/" + upperSymbol.toLowerCase() + "-" + i,
                    date,
                    t[2]
            ));
        }

        log.debug("Returning {} articles for symbol: {}", articles.size(), upperSymbol);
        return articles;
    }

    private List<String[]> getNewsTemplates(String symbol) {
        Map<String, List<String[]>> symbolNews = getSymbolSpecificNews();
        if (symbolNews.containsKey(symbol)) {
            return symbolNews.get(symbol);
        }
        return Arrays.asList(
            new String[]{symbol + " Reports Strong Quarterly Earnings Beat",
                symbol + " exceeded analyst expectations in Q3, reporting earnings growth of 12% year-over-year.",
                "Financial Times"},
            new String[]{symbol + " Expands Market Presence with Strategic Partnership",
                "The company announced a major strategic alliance expected to boost revenue by 8% next fiscal year.",
                "Bloomberg"},
            new String[]{symbol + " Analyst Upgrades to Buy Rating",
                "Leading investment bank raises price target citing improved growth outlook and positive macro tailwinds.",
                "Reuters"},
            new String[]{symbol + " Launches New Product Line Amid Growing Demand",
                "Strong consumer adoption signals positive momentum for the company's upcoming quarterly results.",
                "Economic Times"},
            new String[]{symbol + " Faces Challenges in Global Supply Chain",
                "Rising input costs and supply chain disruptions pose near-term headwinds for profit margins.",
                "WSJ Markets"},
            new String[]{symbol + " Management Raises Full-Year Revenue Guidance",
                "Confident outlook driven by robust order book and expanding client base across key verticals.",
                "CNBC"}
        );
    }

    private Map<String, List<String[]>> getSymbolSpecificNews() {
        Map<String, List<String[]>> news = new HashMap<>();

        news.put("RELIANCE", Arrays.asList(
            new String[]{"Reliance Industries Reports Record Quarterly Profit",
                "Reliance posted record quarterly profits with strong performance across its O2C, digital, and retail segments.",
                "Economic Times"},
            new String[]{"Reliance Jio Crosses 500 Million Subscriber Milestone",
                "Jio's subscriber base hits a historic high, cementing Reliance's dominance in India's digital ecosystem.",
                "Business Standard"},
            new String[]{"Reliance Retail Expands Footprint with 200 New Stores",
                "Reliance Retail continues aggressive expansion across tier-2 and tier-3 cities with strong footfall growth.",
                "Mint"},
            new String[]{"Analysts Upgrade Reliance Industries to Strong Buy",
                "Multiple brokerages raise price targets on Reliance citing new energy segment potential and 5G rollout.",
                "NDTV Profit"},
            new String[]{"Reliance Green Energy Plans $10 Billion Investment",
                "Reliance accelerates transition to renewable energy with massive investment in solar and hydrogen.",
                "Financial Express"},
            new String[]{"Reliance Industries Faces Margin Pressure in Refining Segment",
                "Global oil volatility and refining margin compression weigh on Reliance's petrochemical division.",
                "Reuters India"}
        ));

        news.put("TCS", Arrays.asList(
            new String[]{"TCS Wins $1.5 Billion Deal with UK Government",
                "Tata Consultancy Services secures one of its largest contracts in the UK public sector.",
                "Economic Times"},
            new String[]{"TCS Q3 Revenue Grows 8% Year-on-Year",
                "Strong demand in BFSI and retail verticals drives TCS revenue growth above analyst estimates.",
                "Business Standard"},
            new String[]{"TCS Expands AI and Cloud Practice",
                "TCS launches new AI-powered solutions targeting enterprise transformation and digital migration.",
                "Mint"},
            new String[]{"TCS Headcount Addition Slows Amid Automation Push",
                "TCS reduces fresher hiring targets as automation reduces entry-level IT workforce demand.",
                "NDTV Profit"},
            new String[]{"TCS Shares Rise on Strong Deal Pipeline Visibility",
                "Strong deal wins and pipeline guidance boost investor confidence in TCS growth trajectory.",
                "Moneycontrol"},
            new String[]{"TCS Dividend Announcement Boosts Shareholder Returns",
                "TCS board approves special dividend amid strong cash generation and balance sheet strength.",
                "Financial Express"}
        ));

        news.put("AAPL", Arrays.asList(
            new String[]{"Apple Reports Record iPhone Sales in Q4",
                "Apple's holiday quarter beats expectations with strong iPhone 15 Pro demand driving $120B revenue.",
                "CNBC"},
            new String[]{"Apple Vision Pro Launch Gets Mixed Reception",
                "While groundbreaking, Apple Vision Pro's $3,499 price tag limits near-term volume ambitions.",
                "Bloomberg"},
            new String[]{"Apple Services Revenue Hits All-Time High",
                "App Store, Apple Music, and iCloud drive record services revenue, boosting margins significantly.",
                "WSJ"},
            new String[]{"Apple Expands India Manufacturing Partnership",
                "Apple accelerates India supply chain diversification with Foxconn and Tata deals.",
                "Reuters"},
            new String[]{"Apple Stock Upgraded on Strong AI Pipeline",
                "Analysts bullish on Apple Intelligence as on-device AI differentiation for iPhone upgrade cycle.",
                "Barclays Research"},
            new String[]{"Apple Faces EU Regulatory Pressure on App Store",
                "European regulators push for greater App Store openness under Digital Markets Act compliance.",
                "Financial Times"}
        ));

        news.put("TSLA", Arrays.asList(
            new String[]{"Tesla Cybertruck Deliveries Exceed Expectations",
                "Tesla begins mass Cybertruck delivery with strong initial demand, but production ramp remains key.",
                "Reuters"},
            new String[]{"Tesla Price Cuts Weigh on Margins",
                "Aggressive global price reductions boost volume but compress Tesla's automotive gross margins.",
                "WSJ"},
            new String[]{"Tesla Expands Supercharger Network to Competitors",
                "Tesla's open charging network strategy could become a major revenue driver as adoption grows.",
                "Bloomberg"},
            new String[]{"Tesla Energy Storage Business Hits Record Revenue",
                "Megapack deployments surge as utilities accelerate grid storage investments.",
                "CNBC"},
            new String[]{"Elon Musk's Focus on X Raises Tesla Governance Concerns",
                "Institutional investors worry about CEO bandwidth amid Tesla's critical production phase.",
                "NYT Business"},
            new String[]{"Tesla FSD Beta Progress Encouraging Say Analysts",
                "Full self-driving software improvements fuel long-term robotaxi revenue thesis.",
                "Morgan Stanley"}
        ));

        return news;
    }
}
