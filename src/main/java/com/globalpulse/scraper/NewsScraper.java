package com.globalpulse.scraper;

import com.globalpulse.model.News;
import java.util.List;

public interface NewsScraper {
    List<News> scrape();
    String getSourceName();
}
