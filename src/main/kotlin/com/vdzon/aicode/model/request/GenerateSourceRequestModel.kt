package com.vdzon.aicode.model.request


data class Request(
    val mainBranch: MicroserviceProject?,
    val featureBranch: MicroserviceProject?,
    val storyToImplement: Story
)

