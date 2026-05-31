package com.bbu.springai.springaipractical.dto;

public record ChatRequest(
        String prompt,
        String system,
        String provider,
        String model,
        Double temperature,
        Integer maxCompletionTokens) {
}
