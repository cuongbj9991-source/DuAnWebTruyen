package com.doctruyen.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "stories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "title_alt", length = 255)
    private String titleAlt;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String author;

    @Column(length = 100)
    private String genre;

    @Column(length = 50)
    private String type; // sáng tác, dịch, txt dịch, scan

    @Column(length = 50)
    private String status; // ongoing, completed, paused

    @Column(length = 100)
    private String source; // Falool, Qidian, original, Gutenberg, MangaDex, etc.

    @Column(name = "external_id", length = 255, unique = true)
    private String externalId; // ID from external source (e.g., gutenberg_123)

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "views_total")
    private Long viewsTotal = 0L;

    @Column
    private Integer likes = 0;

    @Column(name = "comments_count")
    private Integer commentsCount = 0;

    @Column
    private Double rating = 0.0;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "last_chapter_updated")
    private LocalDateTime lastChapterUpdated;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
