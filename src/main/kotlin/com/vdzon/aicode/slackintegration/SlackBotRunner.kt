package com.vdzon.aicode.slackintegration

import com.vdzon.aicode.bots.askquestion.QuestionBot
import com.vdzon.aicode.bots.codereview.CodeReviewBot
import com.vdzon.aicode.bots.createbranch.CreateBranchBot
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SlackBotRunner(
    private val slackService: SlackService,
    private val codeReviewBot: CodeReviewBot,
    private val createBranchBot: CreateBranchBot,
    private val questionBot: QuestionBot,
    ) {

    private val log = LoggerFactory.getLogger(SlackBotRunner::class.java)

    /** Elke 3 seconden checken op nieuwe opdrachten **/
    @Scheduled(fixedRate = 3000)
    fun listenForCommands() {
        val command = slackService.getNewCommands() ?: ""
        if (command.startsWith("create_branch:")) {
            log.info("Create branch opdracht ontvangen: $command")
            processCreateBranch(command)
        }
        if (command.startsWith("code_review:")) {
            log.info("Nieuwe code review opdracht ontvangen: $command")
            processCodeReview(command)
        }
        if (command.startsWith("question:")) {
            log.info("Question opdracht ontvangen: $command")
            processQuestion(command)
        }
    }

    private fun processCodeReview(command: String) {
        val details = parseCommand(command)
        val repo = details["repo"] ?: "unknown"
        val sourceFolder = details["sourceFolder"] ?: "unknown"
        val mainbranch = details["mainbranch"] ?: "unknown"
        val featurebranch = details["featurebranch"] ?: "unknown"
        val story = details["story"] ?: "unknown"
        val engine = details["engine"] ?: "unknown"
        val model = details["model"] ?: "unknown"

        val props = details.map { (key, value) -> "$key: $value" }.joinToString("\n")
        slackService.sendMessage("Code review gestart met de volgende properties: \n$props")

        val args = listOf("", repo, sourceFolder, mainbranch, featurebranch, story, engine, model, "")
        val bot = codeReviewBot
        val result = bot.run(args.toTypedArray())
        slackService.sendMessage("code review finished: \n$result")


    }

    private fun processCreateBranch(command: String) {
        val details = parseCommand(command)
        val repo = details["repo"] ?: "unknown"
        val sourceFolder = details["sourceFolder"] ?: "unknown"
        val mainbranch = details["mainbranch"] ?: "unknown"
        val featurebranch = details["featurebranch"] ?: "unknown"
        val story = details["story"] ?: "unknown"
        val engine = details["engine"] ?: "unknown"
        val model = details["model"] ?: "unknown"

        val props = details.map { (key, value) -> "$key: $value" }.joinToString("\n")
        slackService.sendMessage("Creating branch met de volgende properties: \n$props")

        val args = listOf("", repo, sourceFolder, mainbranch, featurebranch, story, engine, model,"")
        val result = createBranchBot.run(args.toTypedArray())
        slackService.sendMessage("Create branch finished: \n$result")
    }

    private fun processQuestion(command: String) {
        val details = parseCommand(command)
        val repo = details["repo"] ?: "unknown"
        val sourceFolder = details["sourceFolder"] ?: "unknown"
        val mainbranch = details["mainbranch"] ?: "unknown"
        val featurebranch = details["featurebranch"] ?: "unknown"
        val story = details["story"] ?: "unknown"
        val engine = details["engine"] ?: "unknown"
        val model = details["model"] ?: "unknown"
        val question = details["question"] ?: "unknown"

        val props = details.map { (key, value) -> "$key: $value" }.joinToString("\n")
        slackService.sendMessage("Code review gestart met de volgende properties: \n$props")

        val args = listOf("", repo, sourceFolder, mainbranch, featurebranch, story, engine, model, question)
        val result = questionBot.run(args.toTypedArray())
        slackService.sendMessage("Processing question finished: \n$result")


    }

    /** Parse een Slack opdracht naar key-value pairs **/
    private fun parseCommand(command: String): Map<String, String> {
        return command.split("\n")
            .filter { it.contains(":") }
            .associate {
                val (key, value) = it.split(":", limit = 2) // Maximaal 2 delen
                key.trim() to value.trim()
            }
    }

}
