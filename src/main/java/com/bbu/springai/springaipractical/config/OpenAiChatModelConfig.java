package com.bbu.springai.springaipractical.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OpenAiChatModelConfig {

    @Bean
    @Primary
    public ChatModel chatModelWithoutDefaultTemperature(OpenAiChatModel openAiChatModel,
                                                       @Value("${spring.ai.openai.chat.options.model}") String defaultModel,
                                                       @Value("${spring.ai.openai.chat.options.max-completion-tokens}") Integer defaultMaxCompletionTokens) {
        return openAiChatModel.mutate()
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(defaultModel)
                        .maxCompletionTokens(defaultMaxCompletionTokens)
                        .build())
                .build();
    }
}
