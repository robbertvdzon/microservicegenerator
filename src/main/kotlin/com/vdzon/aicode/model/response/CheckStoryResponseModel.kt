package com.vdzon.aicode.model.response

data class CheckStoryAiResponse(
    val commentsAboutStory: String,
    val newSuggestedStoryName: String,
    val newSuggestedStoryBody: String,
)