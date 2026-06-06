package com.bbu.springai.springaipractical.service.provider;

import com.bbu.springai.springaipractical.dto.ChatRequest;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

@Component
public class OpenAiChatProvider implements ChatProvider {

    static final String ID = "openai";

    private final OpenAiChatModel chatModel;

    public OpenAiChatProvider(OpenAiChatModel chatModel) {
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
        if (request.hasNoModelOverrides()) {
            return null;
        }

        return OpenAiChatOptions.builder()
                .model(request.model())
                .temperature(request.temperature())
                .maxCompletionTokens(request.maxCompletionTokens())
                .build();
    }

}
