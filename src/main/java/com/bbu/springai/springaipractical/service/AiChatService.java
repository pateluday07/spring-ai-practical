package com.bbu.springai.springaipractical.service;

import com.bbu.springai.springaipractical.dto.ChatRequest;
import reactor.core.publisher.Flux;

public interface AiChatService {

    String chat(ChatRequest request);

    Flux<String> stream(ChatRequest request);

}
