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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class GoogleNewsIndiaScraper implements NewsScraper {
    private static final Logger logger = LoggerFactory.getLogger(GoogleNewsIndiaScraper.class);
    
    private static final Map<String, String> CATEGORY_URLS = new HashMap<>();
    
    static {
        CATEGORY_URLS.put("General", "https://news.google.com/rss?hl=en-IN&gl=IN&ceid=IN:en");
        CATEGORY_URLS.put("Technology", "https://news.google.com/rss/search?q=technology+India&hl=en-IN&gl=IN&ceid=IN:en");
        CATEGORY_URLS.put("Sports", "https://news.google.com/rss/search?q=sports+India&hl=en-IN&gl=IN&ceid=IN:en");
        CATEGORY_URLS.put("Health", "https://news.google.com/rss/search?q=health+India&hl=en-IN&gl=IN&ceid=IN:en");
        CATEGORY_URLS.put("Politics", "https://news.google.com/rss/search?q=politics+India&hl=en-IN&gl=IN&ceid=IN:en");
    }

    // Format for RSS pubDate (e.g., "Wed, 01 Nov 2023 10:00:00 GMT")
    private static final DateTimeFormatter RSS_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

    @Override
    public List<News> scrape() {
        List<News> newsList = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : CATEGORY_URLS.entrySet()) {
            String category = entry.getKey();
            String url = entry.getValue();
            
            try {
                // Using Jsoup XML parser to read the RSS feed
                Document doc = Jsoup.connect(url)
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
                        // Strip HTML tags from description if any, as Google RSS sometimes embeds HTML in description
                        String description = Jsoup.parse(descriptionHtml).text(); 
                        String pubDateStr = item.select("pubDate").text();

                        if (title.isEmpty() || articleUrl.isEmpty()) {
                            continue;
                        }

                        News news = new News();
                        news.setTitle(title);
                        news.setArticleUrl(articleUrl);
                        
                        // Truncate description if it's too long, or handle it
                        if (description.length() > 2000) {
                            description = description.substring(0, 2000) + "...";
                        }
                        news.setDescription(description);
                        
                        // Google News RSS doesn't reliably provide a thumbnail in <image>, we can leave it empty or assign a placeholder later
                        news.setImageUrl(""); 
                        
                        news.setSource(getSourceName());
                        news.setCategory(category);
                        
                        try {
                            LocalDateTime pubDate = LocalDateTime.parse(pubDateStr, RSS_DATE_FORMATTER);
                            news.setPublishedAt(pubDate);
                        } catch (DateTimeParseException e) {
                            news.setPublishedAt(LocalDateTime.now()); // Fallback
                        }
                        
                        double score = 10.0;
                        if (title.toLowerCase().contains("india") || title.toLowerCase().contains("delhi")) {
                            score += 5.0;
                        }
                        news.setPriorityScore(score);
                        news.setTrustScore(90.0);

                        newsList.add(news);
                    } catch (Exception e) {
                        logger.warn("Error parsing individual article on Google News India: {}", e.getMessage());
                    }
                }

            } catch (Exception e) {
                logger.error("Failed to scrape Google News India for category {}: {}", category, e.getMessage());
            }
        }

        return newsList;
    }

    @Override
    public String getSourceName() {
        return "Google News India";
    }
}

