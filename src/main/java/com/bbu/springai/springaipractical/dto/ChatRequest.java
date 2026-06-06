package com.bbu.springai.springaipractical.dto;

import org.springframework.util.StringUtils;

public record ChatRequest(
        String prompt,
        String system,
        String provider,
        String model,
        Double temperature,
        Integer maxCompletionTokens) {

    public boolean hasModelOverrides() {
        return !StringUtils.hasText(model)
                && temperature == null
                && maxCompletionTokens == null;
    }

}
