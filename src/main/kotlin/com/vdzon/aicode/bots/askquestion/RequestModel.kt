package com.vdzon.aicode.bots.askquestion

import com.vdzon.aicode.commonmodel.MicroserviceProject
import com.vdzon.aicode.commonmodel.Story

internal data class Request(
    val mainBranch: MicroserviceProject?,
    val featureBranch: MicroserviceProject?,
    val question: String
)