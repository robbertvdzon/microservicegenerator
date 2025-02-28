package com.vdzon.aicode.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

data class Request(
    val mainBranch: MicroserviceProject?,
    val featureBranch: MicroserviceProject?,
)

data class MicroserviceProject(
    val branch: String,
    val sourceFiles: List<SourceFile>,
    val functionalSpecifications: List<String>,
    val technicalSpecifications: List<String>,
    val stories: List<Story>,
)

data class SourceFile(val path: String, val filename: String, val body: String)

data class SourceFiles(val files: List<SourceFile>)

data class Story(
    val storyname: String,
    val storyDescription: String
)
//---

// Kotlin Data Classes voor JSON Mapping
data class OllamaRequest(
    val model: String,
    val messages: List<Message>,
    val stream: Boolean = false,
    val format: Map<String, Any>
)

data class Message(val role: String, val content: String)

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