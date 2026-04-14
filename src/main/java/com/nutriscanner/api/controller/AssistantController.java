package com.nutriscanner.api.controller;

import com.nutriscanner.api.dto.request.AskRequest;
import com.nutriscanner.api.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/assistant")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssistantController {

    private final AIService aiService;

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@RequestBody AskRequest request) {
        System.out.println("Controller received - barcode: " + request.getBarcode() + " question: " + request.getQuestion());
        String answer = aiService.ask(request.getBarcode(), request.getQuestion());
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}