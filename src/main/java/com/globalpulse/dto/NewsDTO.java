package com.globalpulse.dto;

import com.globalpulse.model.News;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class NewsDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String imageUrl;
    private String source;
    private String articleUrl;
    private LocalDateTime publishedAt;

    public NewsDTO(News news) {
        this.id = news.getId();
        this.title = news.getTitle();
        this.description = news.getDescription();
        this.category = news.getCategory();
        this.imageUrl = news.getImageUrl();
        this.source = news.getSource();
        this.articleUrl = news.getArticleUrl();
        this.publishedAt = news.getPublishedAt();
    }
}
