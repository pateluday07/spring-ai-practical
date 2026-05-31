package com.bbu.springai.springaipractical.service;

import com.bbu.springai.springaipractical.dto.ChatRequest;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiChatServiceImpl implements AiChatService {

    private static final String PROVIDER_OPENAI = "openai";
    private static final String PROVIDER_GEMINI = "gemini";

    private final Map<String, ChatModel> chatModels;
    private final String defaultProvider;
    private final String defaultSystem;

    public AiChatServiceImpl(
            OpenAiChatModel openAiChatModel,
            GoogleGenAiChatModel googleGenAiChatModel,
            @Value("${app.ai.default-provider:openai}") String defaultProvider,
            @Value("${spring.ai.system}") String defaultSystem) {
        this.chatModels = Map.of(
                PROVIDER_OPENAI, openAiChatModel,
                PROVIDER_GEMINI, googleGenAiChatModel
        );
        this.defaultProvider = defaultProvider;
        this.defaultSystem = defaultSystem;
    }

    @Override
    public String chat(ChatRequest request) {
        String provider = resolveProvider(request);
        Prompt prompt = toPrompt(request, provider);
        ChatResponse response = chatModels.get(provider).call(prompt);
        return response.getResult().getOutput().getText();
    }

    @Override
    public Flux<String> stream(ChatRequest request) {
        String provider = resolveProvider(request);
        Prompt prompt = toPrompt(request, provider);
        return chatModels.get(provider).stream(prompt)
                .mapNotNull(response -> {
                    if (response.getResult() == null) {
                        return null;
                    }
                    return response.getResult().getOutput().getText();
                })
                .filter(text -> !text.isEmpty());
    }

    private String resolveProvider(ChatRequest request) {
        String provider = StringUtils.hasText(request.provider()) ? request.provider() : defaultProvider;
        if (!chatModels.containsKey(provider)) {
            throw new IllegalArgumentException(
                    "Unknown provider: " + provider + ". Supported providers: openai, gemini.");
        }
        return provider;
    }

    private Prompt toPrompt(ChatRequest request, String provider) {
        List<Message> messages = new ArrayList<>();

        String system = StringUtils.hasText(request.system()) ? request.system() : defaultSystem;

        messages.add(new SystemMessage(system));
        messages.add(new UserMessage(request.prompt()));

        ChatOptions chatOptions = toOptions(request, provider);

        return new Prompt(messages, chatOptions);
    }

    private ChatOptions toOptions(ChatRequest request, String provider) {
        if (!StringUtils.hasText(request.model())
                && request.temperature() == null
                && request.maxCompletionTokens() == null) {
            return null;
        }

        if (PROVIDER_GEMINI.equals(provider)) {
            return GoogleGenAiChatOptions.builder()
                    .model(request.model())
                    .temperature(request.temperature())
                    .maxOutputTokens(request.maxCompletionTokens())
                    .build();
        }

        return OpenAiChatOptions.builder()
                .model(request.model())
                .temperature(request.temperature())
                .maxCompletionTokens(request.maxCompletionTokens())
                .build();
    }
}
