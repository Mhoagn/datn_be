package com.example.demo.dto.TranscriptDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIServiceRequest {
    @JsonProperty("s3_bucket")
    private String s3Bucket;
    
    @JsonProperty("s3_key")
    private String s3Key;
    
    @JsonProperty("s3_region")
    private String s3Region;
    
    @JsonProperty("aws_access_key")
    private String awsAccessKey;
    
    @JsonProperty("aws_secret_key")
    private String awsSecretKey;
}
