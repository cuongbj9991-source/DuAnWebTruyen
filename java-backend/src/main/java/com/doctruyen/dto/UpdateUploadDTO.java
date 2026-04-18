package com.doctruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUploadDTO {
    private String title;
    private String description;
    private String genre;
    private String content;
    private String coverUrl;
}
