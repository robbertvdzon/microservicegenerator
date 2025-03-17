package com.vdzon.aicode.web

import com.vdzon.aicode.bots.askquestion.QuestionBot
import com.vdzon.aicode.bots.checkstory.CheckStoryBot
import com.vdzon.aicode.bots.codegenerator.CodeGeneratorBot
import com.vdzon.aicode.bots.codereview.CodeReviewBot
import com.vdzon.aicode.bots.createbranch.CreateBranchBot
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/")
class WebController(
) {

    private val log = LoggerFactory.getLogger(WebController::class.java)

    // Serve de HTML-pagina
    @GetMapping
    fun index(model: Model): String {
        return "index" // Dit verwijst naar `index.html` in `src/main/resources/templates/`
    }
}

@RestController
@RequestMapping("/api")
class SlackBotApiController(
    private val createBranchBot: CreateBranchBot,
    private val codeReviewBot: CodeReviewBot,
    private val codeGeneratorBot: CodeGeneratorBot,
    private val questionBot: QuestionBot,
    private val checkStoryBot: CheckStoryBot
) {

    private val log = LoggerFactory.getLogger(SlackBotApiController::class.java)

    @PostMapping("/codereview")
    fun handleCodeReview(@RequestBody request: CodeRequest): String {
        log.info("Code Review ontvangen: $request")
        val args = listOf(
            "",
            request.repo,
            request.sourcefolder,
            request.mainbranch,
            request.featurebranch,
            request.story,
            request.engine,
            request.model,
            request.question,
        )
        val bot = codeReviewBot
        val result = bot.run(args.toTypedArray())
        return result
    }

    @PostMapping("/createbranch")
    fun handleCreateBranch(@RequestBody request: CodeRequest): String {
        log.info("Create Branch ontvangen: $request")
        val args = listOf(
            "",
            request.repo,
            request.sourcefolder,
            request.mainbranch,
            request.featurebranch,
            request.story,
            request.engine,
            request.model,
            request.question,
        )
        val bot = createBranchBot
        val result = bot.run(args.toTypedArray())
        return result
    }

    @PostMapping("/updatebranch")
    fun handleUpdateBranch(@RequestBody request: CodeRequest): String {
        log.info("Update Branch ontvangen: $request")
        val args = listOf(
            "",
            request.repo,
            request.sourcefolder,
            request.mainbranch,
            request.featurebranch,
            request.story,
            request.engine,
            request.model,
            request.question,
        )
        val bot = codeGeneratorBot
        val result = bot.run(args.toTypedArray())
        return result
    }

    @PostMapping("/askquestion")
    fun handleAskQuestion(@RequestBody request: CodeRequest): String {
        log.info("Question ontvangen: $request")
        val args = listOf(
            "",
            request.repo,
            request.sourcefolder,
            request.mainbranch,
            request.featurebranch,
            request.story,
            request.engine,
            request.model,
            request.question,
        )
        val bot = questionBot
        val result = bot.run(args.toTypedArray())
        return result
    }

    @PostMapping("/checkstory")
    fun handleCheckStory(@RequestBody request: CodeRequest): String {
        log.info("Check Story ontvangen: $request")
        val args = listOf(
            "",
            request.repo,
            request.sourcefolder,
            request.mainbranch,
            request.featurebranch,
            request.story,
            request.engine,
            request.model,
            request.question,
        )
        val bot = checkStoryBot
        val result = bot.run(args.toTypedArray())
        return result
    }


}

// Data class voor het ontvangen van JSON-data
data class CodeRequest(
    val repo: String,
    val sourcefolder: String,
    val mainbranch: String,
    val featurebranch: String,
    val story: String,
    val engine: String,
    val model: String,
    val question: String,
)
