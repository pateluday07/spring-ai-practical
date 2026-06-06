package com.bbu.springai.springaipractical.service.provider;

import com.bbu.springai.springaipractical.dto.ChatRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ChatProviderRegistry {

    private final Map<String, ChatProvider> providers;

    public ChatProviderRegistry(List<ChatProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toUnmodifiableMap(ChatProvider::id, Function.identity()));
    }

    public ChatProvider resolve(ChatRequest request, String defaultProviderId) {
        String providerId = StringUtils.hasText(request.provider()) ? request.provider() : defaultProviderId;
        ChatProvider provider = providers.get(providerId);

        if (provider == null) {
            throw new IllegalArgumentException(
                    "Unknown provider: " + providerId + ". Supported providers: " + providers.keySet() + ".");
        }

        return provider;
    }

}
