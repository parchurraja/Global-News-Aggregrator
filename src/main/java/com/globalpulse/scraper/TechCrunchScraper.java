package com.globalpulse.scraper;

import com.globalpulse.model.News;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class TechCrunchScraper implements NewsScraper {
    private static final Logger logger = LoggerFactory.getLogger(TechCrunchScraper.class);
    private static final String RSS_URL = "https://techcrunch.com/feed/";
    private static final DateTimeFormatter RSS_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

    @Override
    public List<News> scrape() {
        List<News> newsList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(RSS_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(10000)
                    .parser(org.jsoup.parser.Parser.xmlParser())
                    .get();

            Elements items = doc.select("item");

            for (Element item : items) {
                try {
                    String title = item.select("title").text();
                    String articleUrl = item.select("link").text();
                    String descriptionHtml = item.select("description").text();
                    String description = Jsoup.parse(descriptionHtml).text();
                    
                    if (title.isEmpty() || articleUrl.isEmpty()) {
                        continue;
                    }

                    News news = new News();
                    news.setTitle(title);
                    news.setArticleUrl(articleUrl);
                    
                    if (description.length() > 2000) {
                        description = description.substring(0, 2000) + "...";
                    }
                    news.setDescription(description);
                    
                    // Simple logic to extract a thumbnail if present in description or set a default
                    news.setImageUrl("https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&q=80&w=600");
                    
                    news.setSource(getSourceName());
                    news.setCategory("Technology");
                    
                    String pubDateStr = item.select("pubDate").text();
                    try {
                        LocalDateTime pubDate = LocalDateTime.parse(pubDateStr, RSS_DATE_FORMATTER);
                        news.setPublishedAt(pubDate);
                    } catch (Exception e) {
                        news.setPublishedAt(LocalDateTime.now());
                    }
                    
                    double score = 10.0;
                    if (title.toLowerCase().contains("ai") || title.toLowerCase().contains("apple")) {
                        score += 5.0;
                    }
                    news.setPriorityScore(score);
                    news.setTrustScore(95.0);

                    newsList.add(news);
                } catch (Exception e) {
                    logger.warn("Error parsing individual article on TechCrunch RSS: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Failed to fetch TechCrunch RSS: {}", e.getMessage());
        }

        return newsList;
    }

    @Override
    public String getSourceName() {
        return "TechCrunch";
    }
}
