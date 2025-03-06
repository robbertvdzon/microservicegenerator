package com.vdzon.aicode.bots.checkstory

internal data class CheckStoryAiResponse(
    val commentsAboutStory: String,
    val newSuggestedStoryName: String,
    val newSuggestedStoryBody: String,
)