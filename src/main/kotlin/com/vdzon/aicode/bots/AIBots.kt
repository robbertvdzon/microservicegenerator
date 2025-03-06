package com.vdzon.aicode.bots

import com.vdzon.aicode.bots.checkstory.CheckStoryService
import com.vdzon.aicode.bots.codegenerator.CodeGeneratorService

object AIBots {
    fun getAllBots(): List<AIBot> {
        return listOf(
            CheckStoryService(),
            CodeGeneratorService()
        )
    }
}