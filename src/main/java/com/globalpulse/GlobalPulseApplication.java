package com.globalpulse;

import com.globalpulse.model.News;
import com.globalpulse.repository.NewsRepository;
import com.globalpulse.service.ScraperService;
import com.globalpulse.scraper.NewsScraper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableAsync
public class GlobalPulseApplication {

	public static void main(String[] args) {
		SpringApplication.run(GlobalPulseApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(NewsRepository newsRepository, ScraperService scraperService, List<NewsScraper> scrapers) {
		return args -> {
			if (newsRepository.count() == 0) {
				System.out.println("Database is empty. Running scrapers to fetch initial data...");
				if (scrapers != null) {
				    for (NewsScraper scraper : scrapers) {
				        try {
				            scraperService.executeScraper(scraper);
				        } catch (Exception e) {
				            System.err.println("Error running scraper on startup: " + e.getMessage());
				        }
				    }
				}
				System.out.println("Initial data fetch complete.");
			}
		};
	}

}
