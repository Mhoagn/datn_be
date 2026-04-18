package com.example.demo.dto.TranscriptDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptSegmentDTO {
    private Double start;
    private Double end;
    private String text;
}
