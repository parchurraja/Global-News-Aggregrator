package com.globalpulse.scraper;

import com.globalpulse.model.News;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class RBIScraper implements NewsScraper {
    private static final Logger logger = LoggerFactory.getLogger(RBIScraper.class);
    
    // We use Google News RSS targeting the RBI portal to reliably bypass bot protection
    private static final String RBI_NEWS_URL = "https://news.google.com/rss/search?q=site:rbi.org.in+OR+%22Reserve+Bank+of+India%22+finance&hl=en-IN&gl=IN&ceid=IN:en";
    
    private static final DateTimeFormatter RSS_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

    @Override
    public List<News> scrape() {
        List<News> newsList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(RBI_NEWS_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(10000)
                    .parser(Parser.xmlParser())
                    .get();

            Elements items = doc.select("item");

            for (Element item : items) {
                try {
                    String title = item.select("title").text();
                    String articleUrl = item.select("link").text();
                    String descriptionHtml = item.select("description").text();
                    String description = Jsoup.parse(descriptionHtml).text();
                    String pubDateStr = item.select("pubDate").text();

                    if (title.isEmpty() || articleUrl.isEmpty()) {
                        continue;
                    }

                    News news = new News();
                    news.setTitle(title);
                    news.setArticleUrl(articleUrl);
                    
                    if (description.length() > 2000) {
                        description = description.substring(0, 2000) + "...";
                    }
                    if (description.isEmpty()) {
                        description = "Finance news update from the Reserve Bank of India.";
                    }
                    news.setDescription(description);
                    
                    // Add a default placeholder image for Finance/RBI news if none is available in RSS
                    news.setImageUrl("https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?auto=format&fit=crop&q=80&w=1000"); 
                    news.setSource(getSourceName());
                    news.setCategory("Finance");
                    
                    try {
                        LocalDateTime pubDate = LocalDateTime.parse(pubDateStr, RSS_DATE_FORMATTER);
                        news.setPublishedAt(pubDate);
                    } catch (DateTimeParseException e) {
                        news.setPublishedAt(LocalDateTime.now());
                    }
                    
                    news.setPriorityScore(15.0); // High priority for RBI news
                    news.setTrustScore(99.0); // High trust for official source

                    newsList.add(news);
                } catch (Exception e) {
                    logger.warn("Error parsing individual article on RBI Scraper: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Failed to scrape RBI News: {}", e.getMessage());
        }

        return newsList;
    }

    @Override
    public String getSourceName() {
        return "RBI Portal";
    }
}
