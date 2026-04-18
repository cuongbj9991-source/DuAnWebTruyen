package com.doctruyen.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "story_uploads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String genre;

    @Column(length = 50)
    private String type; // original, translation, etc.

    @Column(length = 50)
    private String status; // draft, published, pending_review

    @Column(length = 500)
    private String coverUrl;

    @Column(columnDefinition = "TEXT")
    private String content; // Story content as text or HTML

    @Column(name = "is_public", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isPublic = false;

    @Column(name = "is_approved", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isApproved = false;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "views_count", columnDefinition = "BIGINT DEFAULT 0")
    private Long viewsCount = 0L;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = "pending_review";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
