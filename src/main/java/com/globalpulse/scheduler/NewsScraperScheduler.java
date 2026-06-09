package com.globalpulse.scheduler;

import com.globalpulse.scraper.NewsScraper;
import com.globalpulse.service.ScraperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import com.globalpulse.repository.NewsRepository;

@Component
public class NewsScraperScheduler {
    private static final Logger logger = LoggerFactory.getLogger(NewsScraperScheduler.class);

    @Autowired
    private List<NewsScraper> scrapers;

    @Autowired
    private ScraperService scraperService;

    @Autowired
    private NewsRepository newsRepository;

    // Run every 10 minutes (600000 ms)
    @Scheduled(fixedRate = 600000)
    public void scheduleScraping() {
        logger.info("Starting scheduled scraping task...");
        if (scrapers != null) {
            for (NewsScraper scraper : scrapers) {
                scraperService.executeScraper(scraper);
            }
        }
    }

    // Run once a day to clean up old news (older than 30 days)
    @Scheduled(cron = "0 0 0 * * ?") // Midnight every day
    public void scheduleCleanup() {
        logger.info("Starting auto-cleanup of old news...");
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            newsRepository.deleteOldNews(cutoffDate);
            logger.info("Completed auto-cleanup.");
        } catch (Exception e) {
            logger.error("Failed to cleanup old news: {}", e.getMessage());
        }
    }
}
