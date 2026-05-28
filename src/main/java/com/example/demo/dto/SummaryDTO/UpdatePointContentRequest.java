package com.example.demo.dto.SummaryDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePointContentRequest {
    
    @NotBlank(message = "Nội dung không được để trống")
    private String content;
}
