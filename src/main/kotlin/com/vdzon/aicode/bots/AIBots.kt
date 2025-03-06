package com.vdzon.aicode.bots

import com.vdzon.aicode.bots.checkstory.CheckStoryBot
import com.vdzon.aicode.bots.codegenerator.CodeGeneratorBot

object AIBots {
    fun getAllBots(): List<AIBot> {
        return listOf(
            CheckStoryBot(),
            CodeGeneratorBot()
        )
    }
}