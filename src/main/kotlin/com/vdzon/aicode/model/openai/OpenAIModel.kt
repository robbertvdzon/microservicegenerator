package com.vdzon.aicode.model.openai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAIResponse(
    val choices: List<OpenAiChoice>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiChoice(
    val message: OpenAiMessage,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiMessage(
    val role: String,
    val content: String
)