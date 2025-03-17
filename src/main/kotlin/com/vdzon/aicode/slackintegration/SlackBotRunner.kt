package com.vdzon.aicode.slackintegration

import com.vdzon.aicode.bots.codereview.CodeReviewBot
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SlackBotRunner(
    private val slackService: SlackService,
    private val codeReviewBot: CodeReviewBot
    ) {

    private val log = LoggerFactory.getLogger(SlackBotRunner::class.java)

    /** Elke 3 seconden checken op nieuwe opdrachten **/
    @Scheduled(fixedRate = 3000)
    fun listenForCommands() {
        val command = slackService.getNewCommands() ?: ""
        if (command.startsWith("code_review:")) {
            log.info("Nieuwe code review opdracht ontvangen: $command")
            val response = processCodeReview(command)
        }
    }

    /** Simuleert de verwerking van een code review **/
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

        val args = listOf("", repo, sourceFolder, mainbranch, featurebranch, story, engine, model)
        val bot = codeReviewBot
        val result = bot.run(args.toTypedArray())
        slackService.sendMessage("code review finished: \n$result")


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
