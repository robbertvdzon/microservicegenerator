package com.vdzon.aicode.web

import com.vdzon.aicode.bots.codereview.CodeReviewBot
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model

@Controller
@RequestMapping("/")
class WebController {

    private val log = LoggerFactory.getLogger(WebController::class.java)

    // Serve de HTML-pagina
    @GetMapping
    fun index(model: Model): String {
        return "index" // Dit verwijst naar `index.html` in `src/main/resources/templates/`
    }
}

@RestController
@RequestMapping("/api")
class SlackBotApiController() {

    private val log = LoggerFactory.getLogger(SlackBotApiController::class.java)

    @PostMapping("/codereview")
    fun handleCodeReview(@RequestBody request: CodeRequest): String {
        log.info("Code Review ontvangen: $request")


        val args = listOf("",request.repo, request.mainbranch, request.featurebranch, request.story, request.engine, request.model)
        val bot = CodeReviewBot() // TODO: via Spring  inject4eren
        val result = bot.run(args.toTypedArray())

        return "Code Review gestart voor ${request.repo} op ${request.featurebranch}\n$result"
    }

    @PostMapping("/createbranch")
    fun handleCreateBranch(@RequestBody request: CodeRequest): String {
        log.info("Create Branch ontvangen: $request")
        return "Nieuwe branch ${request.featurebranch} aangemaakt voor ${request.repo}"
    }
}

// Data class voor het ontvangen van JSON-data
data class CodeRequest(
    val repo: String,
    val mainbranch: String,
    val featurebranch: String,
    val story: String,
    val engine: String,
    val model: String
)
