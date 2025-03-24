package com.vdzon.aicode.bots.askquestion

import com.vdzon.aicode.commonmodel.MicroserviceProject

internal data class Request(
    val code: MicroserviceProject?,
    val question: String
)