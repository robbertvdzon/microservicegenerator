package com.vdzon.aicode.model

// AI data model
data class AIRequest(
    val model: String,
    val messages: List<Message>,
    val stream: Boolean = false,
    val format: Map<String, Any>
)

data class Message(val role: String, val content: String)

// own data model that we provide to the AI model in the AIRequest

data class Request(
    val mainBranch: MicroserviceProject?,
    val featureBranch: MicroserviceProject?,
    val storyToImplement: Story
)

data class MicroserviceProject(
    val branch: String,
    val sourceFiles: List<SourceFile>,
    val functionalSpecifications: List<String>,
    val technicalSpecifications: List<String>
)

data class Story(
    val title: String,
    val body: String
)

