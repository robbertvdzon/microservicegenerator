package com.vdzon.aicode.model.ollama

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class OllamaResponse(
    val model: String,
    @JsonProperty("created_at") val createdAt: String,
    val message: OllamaMessage,
    @JsonProperty("done_reason") val doneReason: String,
    val done: Boolean,
    @JsonProperty("total_duration") val totalDuration: Long,
    @JsonProperty("load_duration") val loadDuration: Long,
    @JsonProperty("prompt_eval_count") val promptEvalCount: Int,
    @JsonProperty("prompt_eval_duration") val promptEvalDuration: Long,
    @JsonProperty("eval_count") val evalCount: Int,
    @JsonProperty("eval_duration") val evalDuration: Long
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OllamaMessage(
    val role: String,
    val content: String
)