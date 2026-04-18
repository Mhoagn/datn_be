package com.example.demo.dto.SummaryDTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveFinalSummaryRequest {
    
    @NotNull(message = "Meeting record ID không được để trống")
    private Long meetingRecordId;
    
    @NotEmpty(message = "Phải chọn ít nhất một điểm chính")
    private List<Long> selectedPointIds;
    
    @NotNull(message = "Nội dung tóm tắt không được để trống")
    private String finalContent;
}
