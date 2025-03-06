package com.vdzon.aicode.bots.checkstory

import com.vdzon.aicode.commonmodel.MicroserviceProject
import com.vdzon.aicode.commonmodel.Story


data class CheckStoryRequest(
    val mainBranch: MicroserviceProject?,
    val storyToImplement: Story
)

