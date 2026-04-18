package com.doctruyen.controller;

import com.doctruyen.dto.StoryDTO;
import com.doctruyen.service.StoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/stories")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "https://duanwebtruyen-production.up.railway.app"}, 
             allowedHeaders = "*", 
             methods = {org.springframework.web.bind.annotation.RequestMethod.GET, org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.PUT, org.springframework.web.bind.annotation.RequestMethod.DELETE},
             allowCredentials = "true")
public class StoryController {
    private final StoryService storyService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(required = false) String sort) {
        
        Sort.Direction direction = Sort.Direction.DESC;
        String sortBy = "createdAt";
        
        if (sort != null) {
            switch (sort.toLowerCase()) {
                case "views":
                    sortBy = "viewsTotal";
                    break;
                case "rating":
                    sortBy = "rating";
                    break;
                case "updated":
                    sortBy = "lastChapterUpdated";
                    break;
                default:
                    sortBy = "createdAt";
            }
        }

        Pageable pageable = PageRequest.of(page, limit, Sort.by(direction, sortBy));
        Page<StoryDTO> stories = storyService.getAllStories(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", stories.getContent());
        response.put("total", stories.getTotalElements());
        response.put("pages", stories.getTotalPages());
        response.put("currentPage", page);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchStories(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit) {
        
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StoryDTO> stories = storyService.searchStories(keyword, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", stories.getContent());
        response.put("total", stories.getTotalElements());
        response.put("pages", stories.getTotalPages());
        response.put("currentPage", page);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> filterStories(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit) {
        
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StoryDTO> stories = storyService.filterStories(genre, type, status, source, minRating, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", stories.getContent());
        response.put("total", stories.getTotalElements());
        response.put("pages", stories.getTotalPages());
        response.put("currentPage", page);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter-options")
    public ResponseEntity<Map<String, Object>> getFilterOptions() {
        Map<String, Object> response = new HashMap<>();
        response.put("genres", storyService.getAllGenres());
        response.put("story_types", storyService.getAllTypes());
        response.put("statuses", storyService.getAllStatuses());
        response.put("sources", storyService.getAllSources());
        
        // Add sort options
        var sortOptions = java.util.Arrays.asList(
            Map.ofEntries(Map.entry("value", "createdAt"), Map.entry("label", "Mới nhất")),
            Map.ofEntries(Map.entry("value", "viewsTotal"), Map.entry("label", "Lượt xem")),
            Map.ofEntries(Map.entry("value", "rating"), Map.entry("label", "Đánh giá")),
            Map.ofEntries(Map.entry("value", "lastChapterUpdated"), Map.entry("label", "Cập nhật gần đây"))
        );
        response.put("sort_options", sortOptions);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStoryById(@PathVariable Long id) {
        try {
            storyService.incrementViewCount(id);
            StoryDTO story = storyService.getStoryById(id);
            return ResponseEntity.ok(story);
        } catch (RuntimeException e) {
            log.error("Error fetching story {}: {}", id, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", 404);
            return ResponseEntity.status(404).body(error);
        } catch (Exception e) {
            log.error("Unexpected error fetching story {}: {}", id, e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error fetching story: " + e.getMessage());
            error.put("status", 500);
            return ResponseEntity.status(500).body(error);
        }
    }
}
