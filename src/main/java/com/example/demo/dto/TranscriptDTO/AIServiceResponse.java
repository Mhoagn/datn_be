package com.example.demo.dto.TranscriptDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIServiceResponse {
    private String status;
    
    @JsonProperty("transcript_segments")
    private List<TranscriptSegmentDTO> transcriptSegments;
    
    @JsonProperty("full_text")
    private String fullText;
    
    private String summary;
}
