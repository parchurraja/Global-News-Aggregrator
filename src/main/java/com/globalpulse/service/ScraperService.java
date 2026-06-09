package com.globalpulse.service;

import com.globalpulse.model.News;
import com.globalpulse.repository.NewsRepository;
import com.globalpulse.scraper.NewsScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScraperService {
    private static final Logger logger = LoggerFactory.getLogger(ScraperService.class);

    @Autowired
    private NewsRepository newsRepository;

    @Async
    public void executeScraper(NewsScraper scraper) {
        logger.info("Starting scraper: {}", scraper.getSourceName());
        List<News> scrapedNews = scraper.scrape();
        
        int savedCount = 0;
        for (News news : scrapedNews) {
            if (!newsRepository.existsByTitle(news.getTitle())) {
                try {
                    newsRepository.save(news);
                    savedCount++;
                } catch (Exception e) {
                    logger.error("Failed to save news: {}", news.getTitle(), e);
                }
            }
        }
        logger.info("Completed scraper: {}. Saved {} new articles.", scraper.getSourceName(), savedCount);
    }
}
