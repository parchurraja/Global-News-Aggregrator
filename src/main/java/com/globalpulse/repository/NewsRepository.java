package com.globalpulse.repository;

import com.globalpulse.model.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    
    Page<News> findByCategory(String category, Pageable pageable);
    
    Page<News> findBySource(String source, Pageable pageable);
    
    Page<News> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, Pageable pageable);

    boolean existsByArticleUrl(String articleUrl);

    boolean existsByTitleAndSource(String title, String source);

    boolean existsByTitle(String title);

    @Query("SELECT n FROM News n ORDER BY n.priorityScore DESC, n.publishedAt DESC")
    Page<News> findTrendingNews(Pageable pageable);

    @Query("SELECT n FROM News n WHERE n.publishedAt >= :since ORDER BY n.publishedAt DESC")
    Page<News> findLatestNews(LocalDateTime since, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM News n WHERE n.createdAt < :cutoffDate")
    void deleteOldNews(LocalDateTime cutoffDate);
}
