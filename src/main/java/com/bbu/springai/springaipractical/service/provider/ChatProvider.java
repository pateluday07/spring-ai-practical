package com.bbu.springai.springaipractical.service.provider;

import com.bbu.springai.springaipractical.dto.ChatRequest;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

public interface ChatProvider {

    String id();

    ChatModel chatModel();

    ChatOptions toOptions(ChatRequest request);

    default String chat(Prompt prompt) {
        return chatModel().call(prompt).getResult().getOutput().getText();
    }

    default Flux<String> stream(Prompt prompt) {
        return chatModel().stream(prompt)
                .mapNotNull(response -> {
                    response.getResult();
                    return response.getResult().getOutput().getText();
                })
                .filter(text -> !text.isEmpty());
    }

}
