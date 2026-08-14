//package com.micro.onlinecodeeditor.service;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.stereotype.Service;
//
//@Service
//public class AIService {
//
//    private final ChatClient chatClient;
//
//    public AIService(ChatClient.Builder chatClientBuilder) {
//        this.chatClient = chatClientBuilder.build();
//    }
//
//    public String analyzeCode(
//            String language,
//            String code,
//            String error) {
//
//        String prompt = """
//                You are an expert programming mentor.
//
//                Analyze this %s code.
//
//                Code:
//                %s
//
//                Error:
//                %s
//
//                Give the response in this structure:
//
//                1. Error
//                2. Why this error happened
//                3. How to fix it
//                4. Optimized code
//                5. Time complexity
//                6. Space complexity
//                7. Additional suggestions
//
//                Explain in simple beginner-friendly language.
//                """.formatted(language, code, error);
//
//        return chatClient
//                .prompt()
//                .user(prompt)
//                .call()
//                .content();
//    }
//}