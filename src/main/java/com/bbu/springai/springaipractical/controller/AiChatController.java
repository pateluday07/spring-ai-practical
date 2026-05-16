package com.bbu.springai.springaipractical.controller;

import com.bbu.springai.springaipractical.dto.ChatRequest;
import com.bbu.springai.springaipractical.service.AiChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/chat")
public class AiChatController {

    private final AiChatService chatService;

    public AiChatController(AiChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> chat(@RequestBody ChatRequest request) {
        return Map.of("response", chatService.chat(request));
    }

}
