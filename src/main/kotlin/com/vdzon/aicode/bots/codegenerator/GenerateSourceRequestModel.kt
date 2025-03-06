package com.vdzon.aicode.bots.codegenerator

import com.vdzon.aicode.commonmodel.MicroserviceProject
import com.vdzon.aicode.commonmodel.Story


data class Request(
    val mainBranch: MicroserviceProject?,
    val featureBranch: MicroserviceProject?,
    val storyToImplement: Story
)

