package com.example.demo.service.interf;

import com.example.demo.dto.SummaryDTO.FinalSummaryResponse;
import com.example.demo.dto.SummaryDTO.SaveFinalSummaryRequest;
import com.example.demo.dto.SummaryDTO.SummaryPointDTO;
import com.example.demo.dto.SummaryDTO.SummaryResponse;
import com.example.demo.dto.TranscriptDTO.TranscriptResponse;

import java.util.List;

public interface TranscriptInterface {

    void processRecordedVideo(Long recordId, Long currentUserId);

    TranscriptResponse getTranscript(Long recordId);

    List<SummaryResponse> getSummaries(Long recordId);

    SummaryPointDTO updatePointSelection(Long pointId, Boolean isSelected);

    SummaryPointDTO updatePointContent(Long pointId, String content);

    FinalSummaryResponse saveFinalSummary(SaveFinalSummaryRequest request);

    FinalSummaryResponse getFinalSummary(Long recordId);
}
