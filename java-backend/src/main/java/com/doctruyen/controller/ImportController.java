package com.doctruyen.controller;

import com.doctruyen.service.ImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "https://duanwebtruyen-production.up.railway.app"}, 
             allowedHeaders = "*", 
             methods = {org.springframework.web.bind.annotation.RequestMethod.GET, org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.PUT, org.springframework.web.bind.annotation.RequestMethod.DELETE},
             allowCredentials = "true")
public class ImportController {
    
    private final ImportService importService;

    /**
     * Import stories from Project Gutenberg
     * GET /api/import/gutenberg?keyword=pride&limit=50
     */
    @GetMapping("/gutenberg")
    public ResponseEntity<Map<String, Object>> importFromGutenberg(
            @RequestParam(defaultValue = "story") String keyword,
            @RequestParam(defaultValue = "50") int limit) {
        
        log.info("Received import request from Gutenberg: keyword={}, limit={}", keyword, limit);
        
        // Run async import
        importService.importFromGutenberg(keyword, limit);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Gutenberg import started");
        response.put("keyword", keyword);
        response.put("limit", limit);
        response.put("status", "importing");
        
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Import manga from MangaDex
     * GET /api/import/mangadex?keyword=action&limit=50
     */
    @GetMapping("/mangadex")
    public ResponseEntity<Map<String, Object>> importFromMangaDex(
            @RequestParam(defaultValue = "action") String keyword,
            @RequestParam(defaultValue = "50") int limit) {
        
        log.info("Received import request from MangaDex: keyword={}, limit={}", keyword, limit);
        
        // Run async import
        importService.importFromMangaDex(keyword, limit);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "MangaDex import started");
        response.put("keyword", keyword);
        response.put("limit", limit);
        response.put("status", "importing");
        
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Import books from Archive.org (Public Domain)
     * GET /api/import/archive-org?keyword=fiction&limit=50
     */
    @GetMapping("/archive-org")
    public ResponseEntity<Map<String, Object>> importFromArchiveOrg(
            @RequestParam(defaultValue = "fiction") String keyword,
            @RequestParam(defaultValue = "50") int limit) {
        
        log.info("Received import request from Archive.org: keyword={}, limit={}", keyword, limit);
        
        // Run async import
        importService.importFromArchiveOrg(keyword, limit);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Archive.org import started (Public Domain)");
        response.put("keyword", keyword);
        response.put("limit", limit);
        response.put("status", "importing");
        response.put("note", "Archive.org contains only public domain texts - completely legal!");
        
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Import books from OpenLibrary
     * GET /api/import/openlibrary?keyword=fiction&limit=50
     */
    @GetMapping("/openlibrary")
    public ResponseEntity<Map<String, Object>> importFromOpenLibrary(
            @RequestParam(defaultValue = "fiction") String keyword,
            @RequestParam(defaultValue = "50") int limit) {
        
        log.info("Received import request from OpenLibrary: keyword={}, limit={}", keyword, limit);
        
        // Run async import
        importService.importFromOpenLibrary(keyword, limit);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "OpenLibrary import started");
        response.put("keyword", keyword);
        response.put("limit", limit);
        response.put("status", "importing");
        response.put("note", "OpenLibrary is a free and open catalog of books on the web");
        
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Import books from Project Gutenberg with full text content
     * GET /api/import/project-gutenberg?keyword=adventure&limit=10
     */
    @GetMapping("/project-gutenberg")
    public ResponseEntity<Map<String, Object>> importFromProjectGutenberg(
            @RequestParam(defaultValue = "adventure") String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Received import request from Project Gutenberg: keyword={}, limit={}", keyword, limit);
        
        // Run async import
        importService.importFromProjectGutenberg(keyword, limit);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Project Gutenberg import started");
        response.put("keyword", keyword);
        response.put("limit", limit);
        response.put("status", "importing");
        response.put("note", "Project Gutenberg is a collection of 70,000+ free public domain books with full text content");
        
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Get import statistics
     * GET /api/import/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        var stats = importService.getImportStats();
        
        Map<String, Object> response = new HashMap<>();
        response.put("gutenberg", stats.gutenberg);
        response.put("mangaDex", stats.mangaDex);
        response.put("openLibrary", stats.openLibrary);
        response.put("archiveOrg", stats.archiveOrg);
        response.put("total", stats.total);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Clear imported stories from a source
     * POST /api/import/clear?source=Gutenberg
     */
    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearImportedStories(
            @RequestParam String source) {
        
        importService.clearImportedStories(source);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Cleared stories from " + source);
        response.put("source", source);
        
        return ResponseEntity.ok(response);
    }
}
