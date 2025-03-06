package com.vdzon.aicode.model.request


data class CheckStoryRequest(
    val mainBranch: MicroserviceProject?,
    val storyToImplement: Story
)

