package com.bbu.springai.springaipractical.dto;

public record ChatRequest(
        String prompt,
        String system,
        String model,
        Double temperature,
        Integer maxCompletionTokens) {
}
