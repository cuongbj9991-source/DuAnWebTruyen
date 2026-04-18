package com.doctruyen.controller;

import com.doctruyen.dto.StoryUploadDTO;
import com.doctruyen.dto.CreateUploadDTO;
import com.doctruyen.dto.UpdateUploadDTO;
import com.doctruyen.service.StoryUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class StoryUploadController {
    private final StoryUploadService uploadService;

    /**
     * Create new story upload (requires auth)
     */
    @PostMapping
    public ResponseEntity<StoryUploadDTO> createUpload(
            @RequestBody CreateUploadDTO dto,
            @RequestHeader("User-Id") Long userId) {
        try {
            StoryUploadDTO result = uploadService.createUpload(userId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            log.error("Error creating upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get user's own uploads (requires auth)
     */
    @GetMapping("/my-uploads")
    public ResponseEntity<Map<String, Object>> getMyUploads(
            @RequestHeader("User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<StoryUploadDTO> uploads = uploadService.getUserUploads(userId, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("data", uploads.getContent());
            response.put("total", uploads.getTotalElements());
            response.put("pages", uploads.getTotalPages());
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting user uploads: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get published stories from uploads
     */
    @GetMapping("/published")
    public ResponseEntity<Map<String, Object>> getPublishedStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<StoryUploadDTO> stories = uploadService.getPublishedStories(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("data", stories.getContent());
            response.put("total", stories.getTotalElements());
            response.put("pages", stories.getTotalPages());
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting published stories: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search in published stories
     */
    @GetMapping("/published/search")
    public ResponseEntity<Map<String, Object>> searchPublishedStories(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "viewsCount"));
            Page<StoryUploadDTO> stories = uploadService.searchPublishedStories(keyword, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("data", stories.getContent());
            response.put("total", stories.getTotalElements());
            response.put("pages", stories.getTotalPages());
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching stories: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get single upload by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<StoryUploadDTO> getUploadById(@PathVariable Long id) {
        try {
            StoryUploadDTO upload = uploadService.getUploadById(id);
            return ResponseEntity.ok(upload);
        } catch (Exception e) {
            log.error("Error getting upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Update upload (owner only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<StoryUploadDTO> updateUpload(
            @PathVariable Long id,
            @RequestBody UpdateUploadDTO dto,
            @RequestHeader("User-Id") Long userId) {
        try {
            StoryUploadDTO result = uploadService.updateUpload(id, userId, dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error updating upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Delete upload (owner only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUpload(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        try {
            uploadService.deleteUpload(id, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Approve upload (admin only)
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<StoryUploadDTO> approveUpload(@PathVariable Long id) {
        try {
            StoryUploadDTO result = uploadService.approveUpload(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error approving upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Reject upload (admin only)
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<StoryUploadDTO> rejectUpload(
            @PathVariable Long id,
            @RequestParam String reason) {
        try {
            StoryUploadDTO result = uploadService.rejectUpload(id, reason);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error rejecting upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Publish upload (owner only, must be approved first)
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publishUpload(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        try {
            uploadService.publishUpload(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error publishing upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get pending reviews (admin only)
     */
    @GetMapping("/admin/pending-reviews")
    public ResponseEntity<Map<String, Object>> getPendingReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
            Page<StoryUploadDTO> uploads = uploadService.getPendingReviews(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("data", uploads.getContent());
            response.put("total", uploads.getTotalElements());
            response.put("pages", uploads.getTotalPages());
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting pending reviews: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
