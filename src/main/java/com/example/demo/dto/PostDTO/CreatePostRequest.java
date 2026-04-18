package com.example.demo.dto.PostDTO;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequest{

    private Long groupId;

    private String content;

    private List<MultipartFile> files;

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFiles(List<MultipartFile> files) {
        this.files = files;
    }
}
