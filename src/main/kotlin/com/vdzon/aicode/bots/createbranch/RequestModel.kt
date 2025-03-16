package com.vdzon.aicode.bots.createbranch

import com.vdzon.aicode.commonmodel.MicroserviceProject
import com.vdzon.aicode.commonmodel.Story

internal data class Request(
    val mainBranch: MicroserviceProject?,
    val storyToImplement: Story
)

