package com.bbu.springai.springaipractical.service.provider;

import com.bbu.springai.springaipractical.dto.ChatRequest;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;

@Component
public class GeminiChatProvider implements ChatProvider {

    private static final String ID = "gemini";

    private final GoogleGenAiChatModel chatModel;

    public GeminiChatProvider(GoogleGenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ChatModel chatModel() {
        return chatModel;
    }

    @Override
    public ChatOptions toOptions(ChatRequest request) {
        if (request.hasModelOverrides()) {
            return null;
        }

        return GoogleGenAiChatOptions.builder()
                .model(request.model())
                .temperature(request.temperature())
                .maxOutputTokens(request.maxCompletionTokens())
                .build();
    }

}
