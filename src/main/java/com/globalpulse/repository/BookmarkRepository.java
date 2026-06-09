package com.globalpulse.repository;

import com.globalpulse.model.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Page<Bookmark> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT b.news.id FROM Bookmark b WHERE b.user.id = :userId")
    List<Long> findNewsIdsByUserId(@Param("userId") Long userId);

    Optional<Bookmark> findByUserIdAndNewsId(Long userId, Long newsId);
    boolean existsByUserIdAndNewsId(Long userId, Long newsId);
    
    @org.springframework.transaction.annotation.Transactional
    void deleteByUserId(Long userId);
}
