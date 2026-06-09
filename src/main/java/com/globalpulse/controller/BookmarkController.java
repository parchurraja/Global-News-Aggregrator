package com.globalpulse.controller;

import com.globalpulse.dto.MessageResponse;
import com.globalpulse.model.Bookmark;
import com.globalpulse.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    @PostMapping("/{newsId}")
    public ResponseEntity<?> addBookmark(@PathVariable Long newsId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        try {
            bookmarkService.addBookmark(username, newsId);
            return ResponseEntity.ok(new MessageResponse("Bookmark added successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Page<Bookmark>> getUserBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return ResponseEntity.ok(bookmarkService.getUserBookmarks(username, page, size));
    }

    @GetMapping("/ids")
    public ResponseEntity<?> getUserBookmarkIds() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return ResponseEntity.ok(bookmarkService.getUserBookmarkIds(username));
    }

    @DeleteMapping("/{newsId}")
    public ResponseEntity<?> removeBookmark(@PathVariable Long newsId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        try {
            bookmarkService.removeBookmark(username, newsId);
            return ResponseEntity.ok(new MessageResponse("Bookmark removed successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
