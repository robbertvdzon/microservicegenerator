package com.vdzon.aicode.slackintegration

import com.vdzon.aicode.bots.codereview.CodeReviewBot
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

//@Component
class SlackBotRunner(private val slackService: SlackService) {

    private val log = LoggerFactory.getLogger(SlackBotRunner::class.java)

    /** Elke 5 seconden checken op nieuwe opdrachten **/
    @Scheduled(fixedRate = 3000)
    fun listenForCommands() {
        val command = slackService.getNewCommands()?:""
        if (command.startsWith("code_review:")) {
            log.info("Nieuwe code review opdracht ontvangen: $command")
            val response = processCodeReview(command)
        }
    }

    /** Simuleert de verwerking van een code review **/
    private fun processCodeReview(command: String) {
        val details = parseCommand(command)
//        val repo = details["repo"] ?: "unknown"
        val repo = "git@github.com:robbertvdzon/sample-generated-ai-project.git" // TODO Dit parsen!!!!!!!!
        val mainbranch = details["mainbranch"] ?: "unknown"
        val featurebranch = details["featurebranch"] ?: "unknown"
        val story = details["story"] ?: "unknown"
        val engine = details["engine"] ?: "unknown"
        val model = details["model"] ?: "unknown"
        val bot = CodeReviewBot()
        val args = listOf("",repo, mainbranch, featurebranch, story, engine, model)

        val props = details.map { (key, value) -> "$key: $value" }.joinToString("\n")
        slackService.sendMessage("Code review gestart met de volgende properties: \n$props")

        val result = bot.run(args.toTypedArray())
        slackService.sendMessage("code review finished: \n$result")


    }

    /** Parse een Slack opdracht naar key-value pairs **/
    private fun parseCommand(command: String): Map<String, String> {
        return command.split("\n")
            .filter { it.contains(":") }
            .associate {
                val (key, value) = it.split(":")
                key.trim() to value.trim()
            }
    }
}
