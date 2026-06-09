package com.globalpulse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "news", indexes = {
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_source", columnList = "source"),
        @Index(name = "idx_published_at", columnList = "published_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(nullable = false)
    private String source;

    @Column(name = "article_url", nullable = false, unique = true, length = 1000)
    private String articleUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "priority_score")
    private Double priorityScore = 0.0;

    @Column(name = "trust_score")
    private Double trustScore = 100.0;

    @Column(length = 50)
    private String sentiment = "Neutral";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
