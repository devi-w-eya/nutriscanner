package com.nutriscanner.api.controller;

import com.nutriscanner.api.dto.response.HistoryDTO;
import com.nutriscanner.api.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/history")
    public ResponseEntity<List<HistoryDTO>> getHistory(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
                historyService.getHistory(UUID.fromString(userId))
        );
    }
}