package com.bbu.springai.springaipractical.service;

import com.bbu.springai.springaipractical.dto.ChatRequest;
import com.bbu.springai.springaipractical.service.prompt.ChatPromptFactory;
import com.bbu.springai.springaipractical.service.provider.ChatProvider;
import com.bbu.springai.springaipractical.service.provider.ChatProviderRegistry;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiChatServiceImpl implements AiChatService {

    private final ChatProviderRegistry providerRegistry;
    private final ChatPromptFactory promptFactory;
    private final String defaultProviderId;

    public AiChatServiceImpl(
            ChatProviderRegistry providerRegistry,
            ChatPromptFactory promptFactory,
            @Value("${app.ai.default-provider:openai}") String defaultProviderId) {
        this.providerRegistry = providerRegistry;
        this.promptFactory = promptFactory;
        this.defaultProviderId = defaultProviderId;
    }

    @Override
    public String chat(ChatRequest request) {
        ChatProvider provider = providerRegistry.resolve(request, defaultProviderId);
        Prompt prompt = promptFactory.create(request, provider);
        return provider.chat(prompt);
    }

    @Override
    public Flux<String> stream(ChatRequest request) {
        ChatProvider provider = providerRegistry.resolve(request, defaultProviderId);
        Prompt prompt = promptFactory.create(request, provider);
        return provider.stream(prompt);
    }

}
