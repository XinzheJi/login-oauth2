package com.system.ai.controller;

import com.system.ai.domain.vo.ChatRequest;
import com.system.ai.service.AiChatService;
import com.system.login.domain.vo.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/chat")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping
    public ApiResponse<Map<String, String>> chat(@RequestBody ChatRequest request) {
        String reply = aiChatService.chat(request.getMessage());
        return ApiResponse.success(Map.of("reply", reply));
    }
}

