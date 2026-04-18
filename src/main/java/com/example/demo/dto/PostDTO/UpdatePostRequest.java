package com.example.demo.dto.PostDTO;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdatePostRequest {
    private String content;
    private List<MultipartFile> mediaInfoList;
    private List<String> keepMediaUrls;
}
