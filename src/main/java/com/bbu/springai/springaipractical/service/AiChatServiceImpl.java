package com.bbu.springai.springaipractical.service;

import com.bbu.springai.springaipractical.dto.ChatRequest;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiChatServiceImpl implements AiChatService {

    private final ChatModel chatModel;

    private final String defaultModel;

    private final Double defaultTemperature;

    private final Integer defaultMaxTokens;

    private final String defaultSystem;

    public AiChatServiceImpl(ChatModel chatModel,
                             @Value("${spring.ai.openai.chat.options.model}") String defaultModel,
                             @Value("${spring.ai.openai.chat.options.temperature}") Double defaultTemperature,
                             @Value("${spring.ai.openai.chat.options.max-tokens}") Integer defaultMaxTokens,
                             @Value("${spring.ai.system}") String defaultSystem) {
        this.chatModel = chatModel;
        this.defaultModel = defaultModel;
        this.defaultTemperature = defaultTemperature;
        this.defaultMaxTokens = defaultMaxTokens;
        this.defaultSystem = defaultSystem;
    }

    @Override
    public String chat(ChatRequest request) {
        Prompt prompt = toPrompt(request);
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    private Prompt toPrompt(ChatRequest request) {
        List<Message> messages = new ArrayList<>();

        String system = StringUtils.hasText(request.system()) ? request.system() : defaultSystem;
        messages.add(new SystemMessage(system));
        messages.add(new UserMessage(request.prompt()));

        ChatOptions chatOptions = toOptions(request);

        return new Prompt(messages, chatOptions);
    }

    private ChatOptions toOptions(ChatRequest request) {
        return ChatOptions.builder()
                .model(StringUtils.hasText(request.model()) ? request.model() : defaultModel)
                .temperature(request.temperature() != null ? request.temperature() : defaultTemperature)
                .maxTokens(request.maxTokens() != null ? request.maxTokens() : defaultMaxTokens)
                .build();
    }
}
