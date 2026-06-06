package com.bbu.springai.springaipractical.service.prompt;

import com.bbu.springai.springaipractical.dto.ChatRequest;
import com.bbu.springai.springaipractical.service.provider.ChatProvider;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChatPromptFactory {

    private final String defaultSystem;

    public ChatPromptFactory(@Value("${spring.ai.system}") String defaultSystem) {
        this.defaultSystem = defaultSystem;
    }

    public Prompt create(ChatRequest request, ChatProvider provider) {
        List<Message> messages = new ArrayList<>();

        String system = StringUtils.hasText(request.system()) ? request.system() : defaultSystem;
        messages.add(new SystemMessage(system));
        messages.add(new UserMessage(request.prompt()));

        return new Prompt(messages, provider.toOptions(request));
    }

}
