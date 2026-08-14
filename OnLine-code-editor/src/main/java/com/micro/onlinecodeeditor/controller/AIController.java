//package com.micro.onlinecodeeditor.controller;
//
//import com.micro.onlinecodeeditor.dto.AIAnalysisRequest;
//import com.micro.onlinecodeeditor.service.AIService;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/ai")
//@CrossOrigin(origins = "http://localhost:5173")
//public class AIController {
//
//    private final AIService aiService;
//
//    public AIController(AIService aiService) {
//        this.aiService = aiService;
//    }
//
//    @PostMapping("/analyze")
//    public String analyzeCode(
//            @RequestBody AIAnalysisRequest request) {
//
//        return aiService.analyzeCode(
//                request.getLanguage(),
//                request.getCode(),
//                request.getError()
//        );
//    }
//}