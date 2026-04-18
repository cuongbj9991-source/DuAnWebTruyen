package com.doctruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryDTO {
    private Long id;
    private String title;
    private String titleAlt;
    private String description;
    private String author;
    private String genre;
    private String type;
    private String status;
    private String source;
    private String coverUrl;
    private Long viewsTotal;
    private Integer likes;
    private Integer commentsCount;
    private Double rating;
    private Integer ratingCount;
    private LocalDateTime lastChapterUpdated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
