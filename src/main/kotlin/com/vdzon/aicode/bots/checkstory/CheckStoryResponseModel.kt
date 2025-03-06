package com.vdzon.aicode.bots.checkstory

data class CheckStoryAiResponse(
    val commentsAboutStory: String,
    val newSuggestedStoryName: String,
    val newSuggestedStoryBody: String,
)