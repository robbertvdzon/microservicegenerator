package com.vdzon.aicode.model.openai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAIResponse(
    val choices: List<OpenAiChoice>,
    val usage: Usage,
    val model: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
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