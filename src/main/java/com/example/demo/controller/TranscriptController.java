package com.example.demo.controller;

import com.example.demo.dto.SummaryDTO.*;
import com.example.demo.dto.TranscriptDTO.TranscriptResponse;
import com.example.demo.service.impl.TranscriptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transcripts")
@RequiredArgsConstructor
public class TranscriptController {
    
    private final TranscriptService transcriptService;
    
    /**
     * Lấy transcript của một meeting record
     * GET /transcripts/record/{recordId}
     */
    @GetMapping("/record/{recordId}")
    public ResponseEntity<TranscriptResponse> getTranscript(@PathVariable Long recordId) {
        TranscriptResponse transcript = transcriptService.getTranscript(recordId);
        return ResponseEntity.ok(transcript);
    }
    
    /**
     * Lấy tất cả summaries của một meeting record (chỉ có 1 với model QWEN)
     * GET /transcripts/record/{recordId}/summaries
     */
    @GetMapping("/record/{recordId}/summaries")
    public ResponseEntity<List<SummaryResponse>> getSummaries(@PathVariable Long recordId) {
        List<SummaryResponse> summaries = transcriptService.getSummaries(recordId);
        return ResponseEntity.ok(summaries);
    }
    
    /**
     * Cập nhật trạng thái isSelected của một điểm tóm tắt
     * PATCH /transcripts/points/{pointId}/selection
     */
    @PatchMapping("/points/{pointId}/selection")
    public ResponseEntity<SummaryPointDTO> updatePointSelection(
            @PathVariable Long pointId,
            @RequestBody @Valid UpdatePointSelectionRequest request) {
        SummaryPointDTO updated = transcriptService.updatePointSelection(pointId, request.getIsSelected());
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Lưu final summary (người dùng chọn các điểm và tạo tóm tắt cuối cùng)
     * POST /transcripts/record/{recordId}/final-summary
     */
    @PostMapping("/record/{recordId}/final-summary")
    public ResponseEntity<FinalSummaryResponse> saveFinalSummary(
            @PathVariable Long recordId,
            @RequestBody @Valid SaveFinalSummaryRequest request) {
        

        
        // Đảm bảo recordId trong path và body khớp nhau
        if (!recordId.equals(request.getMeetingRecordId())) {
            throw new IllegalArgumentException("Record ID không khớp");
        }
        
        FinalSummaryResponse response = transcriptService.saveFinalSummary(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Lấy final summary của một meeting record
     * GET /transcripts/record/{recordId}/final-summary
     */
    @GetMapping("/record/{recordId}/final-summary")
    public ResponseEntity<FinalSummaryResponse> getFinalSummary(@PathVariable Long recordId) {
        FinalSummaryResponse finalSummary = transcriptService.getFinalSummary(recordId);
        return ResponseEntity.ok(finalSummary);
    }
    
    /**
     * Trigger xử lý AI cho một meeting record (manual trigger hoặc retry)
     * POST /transcripts/record/{recordId}/process
     */
    @PostMapping("/record/{recordId}/process")
    public ResponseEntity<Map<String, String>> triggerProcessing(@PathVariable Long recordId) {
        transcriptService.processRecordedVideo(recordId);
        return ResponseEntity.accepted().body(Map.of(
            "status", "processing",
            "message", "Đã khởi tạo xử lý AI cho bản ghi. Vui lòng đợi vài phút."
        ));
    }
}
