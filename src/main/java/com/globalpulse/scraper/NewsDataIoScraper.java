package com.globalpulse.scraper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globalpulse.model.News;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class NewsDataIoScraper implements NewsScraper {
    private static final Logger logger = LoggerFactory.getLogger(NewsDataIoScraper.class);
    
    // Fallback/demo key, in production this should be in application.yml
    private static final String API_KEY = "pub_demo_key_or_replace_me";
    private static final String API_URL = "https://newsdata.io/api/1/news?apikey=" + API_KEY + "&q=technology,sports,business,health,entertainment";

    private final RestTemplate restTemplate;

    public NewsDataIoScraper() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public List<News> scrape() {
        List<News> newsList = new ArrayList<>();
        try {
            logger.info("Fetching news from NewsData.io API...");
            String response = restTemplate.getForObject(API_URL, String.class);
            
            if (response != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response);
                JsonNode results = root.path("results");
                
                if (results.isArray()) {
                    for (JsonNode article : results) {
                        News news = new News();
                        news.setTitle(article.path("title").asText(""));
                        
                        // Content or description
                        String description = article.path("description").asText("");
                        if (description.isEmpty() || description.equals("null")) {
                            description = article.path("content").asText("");
                        }
                        // Truncate if too long
                        if (description.length() > 500) {
                            description = description.substring(0, 497) + "...";
                        }
                        news.setDescription(description);
                        
                        String imageUrl = article.path("image_url").asText("");
                        if (!imageUrl.equals("null") && !imageUrl.isEmpty()) {
                            news.setImageUrl(imageUrl);
                        }
                        
                        news.setArticleUrl(article.path("link").asText(""));
                        news.setSource(article.path("source_id").asText("NewsData.io"));
                        
                        // Parse category
                        JsonNode categoryNode = article.path("category");
                        String category = "General";
                        if (categoryNode.isArray() && categoryNode.size() > 0) {
                            category = categoryNode.get(0).asText();
                            // Simple mapping
                            if (category.equalsIgnoreCase("technology")) category = "Technology";
                            else if (category.equalsIgnoreCase("sports")) category = "Sports";
                            else if (category.equalsIgnoreCase("business")) category = "Finance";
                            else if (category.equalsIgnoreCase("health")) category = "Health";
                            else if (category.equalsIgnoreCase("politics")) category = "Politics";
                        }
                        news.setCategory(category);
                        
                        news.setPublishedAt(LocalDateTime.now()); // Fallback to now
                        news.setPriorityScore(15.0); // Medium priority
                        newsList.add(news);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching from NewsData.io: {}", e.getMessage());
        }
        return newsList;
    }

    @Override
    public String getSourceName() {
        return "NewsData.io API";
    }
}
