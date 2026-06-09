package com.globalpulse.service;

import com.globalpulse.dto.ManualNewsRequest;
import com.globalpulse.dto.NewsDTO;
import com.globalpulse.model.News;
import com.globalpulse.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;

    @Cacheable(value = "allNews")
    public Page<NewsDTO> getAllNews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return newsRepository.findAll(pageable).map(NewsDTO::new);
    }

    @Cacheable(value = "newsByCategory")
    public Page<NewsDTO> getNewsByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return newsRepository.findByCategory(category, pageable).map(NewsDTO::new);
    }

    @Cacheable(value = "searchNews")
    public Page<NewsDTO> searchNews(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return newsRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable).map(NewsDTO::new);
    }

    @Cacheable(value = "trendingNews")
    public Page<NewsDTO> getTrendingNews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepository.findTrendingNews(pageable).map(NewsDTO::new);
    }

    @Cacheable(value = "latestNews")
    public Page<NewsDTO> getLatestNews(int page, int size) {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        Pageable pageable = PageRequest.of(page, size);
        return newsRepository.findLatestNews(yesterday, pageable).map(NewsDTO::new);
    }

    @Cacheable(value = "newsBySource")
    public Page<NewsDTO> getNewsBySource(String source, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return newsRepository.findBySource(source, pageable).map(NewsDTO::new);
    }

    @CacheEvict(value = {"allNews", "latestNews", "newsByCategory"}, allEntries = true)
    public News addManualNews(ManualNewsRequest request) {
        News news = new News();
        news.setTitle(request.getTitle());
        news.setSource(request.getSource());
        news.setArticleUrl(request.getUrl());
        news.setCategory(request.getCategory());
        news.setPublishedAt(LocalDateTime.now());
        news.setCreatedAt(LocalDateTime.now());
        news.setDescription("Manually added news article.");
        news.setSentiment("Neutral");
        return newsRepository.save(news);
    }
}
