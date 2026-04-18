package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class MediaInfo {
    private String url;
    private String publicId;
    private String originalName;
    private Long size;
    private String mimeType;
}
