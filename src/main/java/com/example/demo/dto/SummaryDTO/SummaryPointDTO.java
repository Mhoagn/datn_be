package com.example.demo.dto.SummaryDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryPointDTO {
    private Long id;
    private String content;
    private Integer orderIndex;
    private Boolean isSelected;
}
