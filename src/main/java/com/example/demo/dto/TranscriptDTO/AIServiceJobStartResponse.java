package com.example.demo.dto.TranscriptDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AIServiceJobStartResponse {

    @JsonProperty("job_id")
    private String jobId;

    private String status;
}
