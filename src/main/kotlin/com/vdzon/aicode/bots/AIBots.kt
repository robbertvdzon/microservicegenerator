package com.vdzon.aicode.bots

import com.vdzon.aicode.bots.checkstory.CheckStoryBot
import com.vdzon.aicode.bots.codegenerator.CodeGeneratorBot
import com.vdzon.aicode.bots.codereview.CodeReviewBot
import com.vdzon.aicode.bots.createbranch.CreateBranchBot

object AIBots {
    fun getAllBots(): List<AIBot> {
        return listOf(
            CheckStoryBot(),
            CodeGeneratorBot(),
            CodeReviewBot(),
            CreateBranchBot()
        )
    }
}