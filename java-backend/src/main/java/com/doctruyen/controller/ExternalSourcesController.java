package com.doctruyen.controller;

import com.doctruyen.service.MultiSourceSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/external-sources")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class ExternalSourcesController {
    private final MultiSourceSearchService multiSourceSearchService;

    /**
     * Search across all external sources (Gutenberg, OpenLibrary, MangaDex)
     */
    @GetMapping("/search")
    public ResponseEntity<MultiSourceSearchService.MultiSourceSearchResult> searchAllSources(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page) {
        try {
            MultiSourceSearchService.MultiSourceSearchResult result = 
                    multiSourceSearchService.searchAllSources(keyword, page);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error searching external sources: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get recommended books from external sources
     */
    @GetMapping("/recommended")
    public ResponseEntity<MultiSourceSearchService.MultiSourceSearchResult> getRecommended() {
        try {
            MultiSourceSearchService.MultiSourceSearchResult result = 
                    multiSourceSearchService.getRecommendedBooks();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting recommended books: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Search only Gutenberg
     */
    @GetMapping("/gutenberg/search")
    public ResponseEntity<?> searchGutenberg(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok("Use /search endpoint to query all sources including Gutenberg");
    }
}
