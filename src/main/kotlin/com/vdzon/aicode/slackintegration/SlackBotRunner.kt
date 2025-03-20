package com.vdzon.aicode.slackintegration

import com.vdzon.aicode.bots.askquestion.QuestionBot
import com.vdzon.aicode.bots.codereview.CodeReviewBot
import com.vdzon.aicode.bots.createbranch.CreateBranchBot
import jakarta.annotation.PostConstruct
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
    private var lastContext: Context? = null

    @PostConstruct
    fun init() {
        println("Find last context")
        val messages = slackService.getAllCommands()
        val lastContext = messages.filter { it.startsWith("context:") }.firstOrNull()
        println("last context: "+lastContext)
        if (lastContext!=null) processContext(lastContext)
    }

    /** Elke 3 seconden checken op nieuwe opdrachten **/
    @Scheduled(fixedRate = 3000)
    fun listenForCommands() {
        val command = slackService.getNewCommands() ?: ""
        if (command.startsWith("context:")) {
            log.info("Context opdracht ontvangen: $command")
            processContext(command)
            slackService.sendMessage("Context updated")
        }
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
        if (command.startsWith("showcontext:")) {
            log.info("Question opdracht ontvangen: $command")
            processShowContext(command)
        }
        if (command.startsWith("help:")) {
            log.info("Question opdracht ontvangen: $command")
            processShowHelp(command)
        }
    }

    private fun processShowHelp(command: String) {
        val message = """
            available commands:
            context:
            create_branch:
            code_review:
            showcontext:
            help:
            
            Engines to choose from:
            OPEN_AI, OLLAMA
            
            OPEN_AI Models to choose from:
            gpt-4.5-preview
            gpt-4o
            gpt-3.5-turbo
            
            OLLAMA Models to choose from:
            qwen2.5-coder:32b
            qwen2.5-coder:14b
            qwen2.5-coder:7b
            
        """.trimIndent()
        slackService.sendMessage(message)
    }

    private fun processShowContext(command: String) {
        if (lastContext == null) {
            slackService.sendMessage("no context found")
            return
        }
        val message = """
            current context:
            
            context:
            repo: ${lastContext?.repo}
            sourceFolder: ${lastContext?.sourceFolder}
            mainbranch: ${lastContext?.mainbranch}
            featurebranch: ${lastContext?.featurebranch}
            story: ${lastContext?.story}
            engine: ${lastContext?.engine}
            model: ${lastContext?.model}
        """.trimIndent()
        slackService.sendMessage(message)
    }

    private fun processCodeReview(command: String) {
        if (lastContext == null) {
            slackService.sendMessage("No context")
            return
        }
        slackService.sendMessage("Working on code review")
        val args = listOf("", lastContext!!.repo, lastContext!!.sourceFolder, lastContext!!.mainbranch, lastContext!!.featurebranch, lastContext!!.story, lastContext!!.engine, lastContext!!.model, "")
        try {
            val result = codeReviewBot.run(args.toTypedArray())
            slackService.sendMessage(result)
        }
        catch (e: Exception) {
            slackService.sendMessage(e.message ?: "")
        }
    }


    private fun processContext(command: String) {
        val details = parseCommand(command)
        val repo = details["repo"] ?: "unknown"
        val sourceFolder = details["sourceFolder"] ?: "unknown"
        val mainbranch = details["mainbranch"] ?: "unknown"
        val featurebranch = details["featurebranch"] ?: "unknown"
        val story = details["story"] ?: "unknown"
        val engine = details["engine"] ?: "unknown"
        val model = details["model"] ?: "unknown"
        lastContext = Context(
            repo, sourceFolder, mainbranch, featurebranch, story, engine, model
        )
    }

    private fun processCreateBranch(command: String) {
        if (lastContext == null) {
            slackService.sendMessage("No context")
            return
        }
        slackService.sendMessage("Creating branch")
        val args = listOf("", lastContext!!.repo, lastContext!!.sourceFolder, lastContext!!.mainbranch, lastContext!!.featurebranch, lastContext!!.story, lastContext!!.engine, lastContext!!.model, "")
        try {
            val result = createBranchBot.run(args.toTypedArray())
            slackService.sendMessage(result)
        }
        catch (e: Exception) {
            slackService.sendMessage(e.message ?: "")
        }
    }

    private fun processQuestion(command: String) {
        if (lastContext == null) {
            slackService.sendMessage("No context")
            return
        }
        slackService.sendMessage("Working on question")
        val details = parseCommand(command)
        val question = details["question"] ?: "unknown"
        val args = listOf("", lastContext!!.repo, lastContext!!.sourceFolder, lastContext!!.mainbranch, lastContext!!.featurebranch, lastContext!!.story, lastContext!!.engine, lastContext!!.model, question)
        try {
            val result = questionBot.run(args.toTypedArray())
            slackService.sendMessage(result)
        }
        catch (e: Exception) {
            slackService.sendMessage(e.message ?: "")
        }
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

    data class Context(
        val repo: String,
        val sourceFolder: String,
        val mainbranch: String,
        val featurebranch: String,
        val story: String,
        val engine: String,
        val model: String,
    )

}


