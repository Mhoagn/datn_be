package com.example.demo.dto.TranscriptDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response từ GET /job-status/{job_id} của AI-service.
 * status: "processing" | "completed" | "failed"
 */
@Data
@NoArgsConstructor
public class AIServiceJobStatusResponse {

    @JsonProperty("job_id")
    private String jobId;

    private String status;

    private AIServiceResponse result;

    private String error;
}
