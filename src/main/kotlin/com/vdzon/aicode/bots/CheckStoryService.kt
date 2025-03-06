package com.vdzon.aicode.bots

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.GithubService
import com.vdzon.aicode.aiengine.AiEngineFactory
import com.vdzon.aicode.model.request.CheckStoryRequest
import com.vdzon.aicode.model.response.AiResponse
import com.vdzon.aicode.model.request.Request
import com.vdzon.aicode.model.response.CheckStoryAiResponse
import com.vdzon.aicode.token.CheckStoryTokens
import com.vdzon.aicode.token.CodeGeneratorTokens


class CheckStoryService(
    val githubService: GithubService,
    val repo: String,
    val mainbranch: String,
    val story: String,
    val engine: String,
    val model: String
) {
    private val tokenGenerator = CheckStoryTokens()
    private val aiEngine= AiEngineFactory.getAiEngine(engine, model)

    fun generateCode() {
        println("\nStart generating code..")
        val storyToImplement = githubService.getTicket(repo, story)
        println("Story to implement: ${storyToImplement.title}")
        println("${storyToImplement.body}")
        val mainCode = githubService.getSerializedRepo(mainbranch)
        println("calling AI model..")

        val startTime = System.currentTimeMillis()
        val requestModel = CheckStoryRequest(mainCode!!, storyToImplement)
        val requestJson = jacksonObjectMapper().writeValueAsString(requestModel)
        val jsonSchema: Map<String, Any> = aiEngine.generateJsonSchemaAsMap(CheckStoryAiResponse::class.java)
        val jsonResponse = aiEngine.chat(jsonSchema, tokenGenerator.getSystemToken(), tokenGenerator.getUserToken(requestJson))
        val aiResponse: CheckStoryAiResponse = jacksonObjectMapper().readValue(jsonResponse,object : TypeReference<CheckStoryAiResponse>() {})

        val endTime = System.currentTimeMillis()

        println("Ai finished in: ${endTime - startTime} ms")

        println("\nComments about story: ")
        println(aiResponse.commentsAboutStory)
        println("\nNew suggested story name: ")
        println(aiResponse.newSuggestedStoryName)
        println("\nNew suggested body: ")
        println(aiResponse.newSuggestedStoryBody)
    }


}
