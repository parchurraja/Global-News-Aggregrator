package com.globalpulse.controller;

import com.globalpulse.dto.ManualNewsRequest;
import com.globalpulse.dto.NewsDTO;
import com.globalpulse.service.NewsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping
    public ResponseEntity<Page<NewsDTO>> getAllNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(newsService.getAllNews(page, size));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<NewsDTO>> getNewsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(newsService.getNewsByCategory(category, page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<NewsDTO>> searchNews(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(newsService.searchNews(keyword, page, size));
    }

    @GetMapping("/trending")
    public ResponseEntity<Page<NewsDTO>> getTrendingNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(newsService.getTrendingNews(page, size));
    }

    @GetMapping("/latest")
    public ResponseEntity<Page<NewsDTO>> getLatestNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(newsService.getLatestNews(page, size));
    }

    @GetMapping("/source/{source}")
    public ResponseEntity<Page<NewsDTO>> getNewsBySource(
            @PathVariable String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(newsService.getNewsBySource(source, page, size));
    }

    @PostMapping("/manual")
    public ResponseEntity<?> manualAddNews(@Valid @RequestBody ManualNewsRequest request) {
        return ResponseEntity.ok(newsService.addManualNews(request));
    }
}
