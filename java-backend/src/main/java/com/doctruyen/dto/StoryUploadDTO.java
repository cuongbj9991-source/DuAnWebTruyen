package com.doctruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryUploadDTO {
    private Long id;
    private Long userId;
    private String title;
    private String author;
    private String description;
    private String genre;
    private String type;
    private String status;
    private String coverUrl;
    private Boolean isPublic;
    private Boolean isApproved;
    private String rejectionReason;
    private Long viewsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;
}
