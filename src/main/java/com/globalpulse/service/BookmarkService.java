package com.globalpulse.service;

import com.globalpulse.model.Bookmark;
import com.globalpulse.model.News;
import com.globalpulse.model.User;
import com.globalpulse.repository.BookmarkRepository;
import com.globalpulse.repository.NewsRepository;
import com.globalpulse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookmarkService {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NewsRepository newsRepository;

    public Bookmark addBookmark(String username, Long newsId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (bookmarkRepository.existsByUserIdAndNewsId(user.getId(), newsId)) {
            throw new RuntimeException("Bookmark already exists");
        }

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News not found"));

        Bookmark bookmark = new Bookmark();
        bookmark.setUser(user);
        bookmark.setNews(news);

        return bookmarkRepository.save(bookmark);
    }

    public Page<Bookmark> getUserBookmarks(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return bookmarkRepository.findByUserId(user.getId(), pageable);
    }

    public List<Long> getUserBookmarkIds(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookmarkRepository.findNewsIdsByUserId(user.getId());
    }

    public void removeBookmark(String username, Long newsId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Bookmark bookmark = bookmarkRepository.findByUserIdAndNewsId(user.getId(), newsId)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));

        bookmarkRepository.delete(bookmark);
    }
}
