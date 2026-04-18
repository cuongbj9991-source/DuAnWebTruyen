package com.doctruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUploadDTO {
    private String title;
    private String author;
    private String description;
    private String genre;
    private String type;
    private String coverUrl;
    private String content;
}
