package com.example.demo.dto.SummaryDTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePointSelectionRequest {
    
    @NotNull(message = "Trạng thái selected không được để trống")
    private Boolean isSelected;
}
