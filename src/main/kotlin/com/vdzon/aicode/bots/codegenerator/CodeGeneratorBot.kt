package com.vdzon.aicode.bots.codegenerator

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.git.GithubService
import com.vdzon.aicode.aiengine.AiEngineFactory
import com.vdzon.aicode.aiengine.util.JsonSchemaHelper
import com.vdzon.aicode.bots.AIBot
import com.vdzon.aicode.commonmodel.SourceFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class CodeGeneratorBot(
): AIBot {
    override fun getName(): String = "implement_story"
    override fun getDescription(): String = "Implement a story"
    override fun getHelp(): String = "implement_story githubrepo mainbranch featurebranch story engine model"
    override fun run(args: Array<String>): String{
        val repo = args.getOrNull(1) ?: throw RuntimeException("Invalid repo")
        val sourceFolder = args.getOrNull(2) ?: throw RuntimeException("Invalid sourceFolder")
        val mainbranch = args.getOrNull(3) ?: throw RuntimeException("Invalid main branch")
        val featurebranch = args.getOrNull(4) ?: throw RuntimeException("feature branch")
        val story = args.getOrNull(5) ?: throw RuntimeException("Invalid story")
        val engine = args.getOrNull(6) ?: throw RuntimeException("Invalid engine")
        val model = args.getOrNull(7) ?: throw RuntimeException("Invalid model")

        val tokenGenerator = Tokens()
        val aiEngine= AiEngineFactory.getAiEngine(engine, model)
        val githubService =  GithubService()

        println("\nStart generating code..")
        val storyToImplement = githubService.getTicket(repo, story)
        val mainCode = githubService.getSerializedRepo(repo, mainbranch, sourceFolder)
        val branch = githubService.getSerializedRepo(repo, featurebranch, sourceFolder)
        println("calling AI model..")

        val startTime = System.currentTimeMillis()
        val requestModel = Request(mainCode!!, branch!!, storyToImplement)
        val requestJson = jacksonObjectMapper().writeValueAsString(requestModel)
        val jsonSchema: Map<String, Any> = JsonSchemaHelper.generateJsonSchemaAsMap(AiResponse::class.java)
        val jsonResponse = aiEngine.chat(jsonSchema, tokenGenerator.getSystemToken(), tokenGenerator.getUserToken(requestJson))
        val aiResponse: AiResponse = jacksonObjectMapper().readValue(jsonResponse,object : TypeReference<AiResponse>() {})
        val endTime = System.currentTimeMillis()

        val git = githubService.cloneRepo(
            repo,
            featurebranch,
            "/tmp/ai-repo"
        )
        println("Saving and pushing generated files..")
        saveGeneratedFiles(sourceFolder, aiResponse.newSourceFiles)
        saveGeneratedFiles(sourceFolder, aiResponse.modifiedSourceFiles)
        githubService.addToGit(git, aiResponse.newSourceFiles.map { it.sourceFilename },sourceFolder)
        githubService.addToGit(git, aiResponse.modifiedSourceFiles.map { it.sourceFilename },sourceFolder)
        githubService.removeFromGit(git, aiResponse.removedSourceFiles,sourceFolder)
        githubService.commit(git, aiResponse.commitMessage)
        githubService.push("/tmp/ai-repo")



        val output = buildString {
            append("Ai finished in: ${endTime - startTime} ms\n\n")
            append("\nExplanation from AI:")
            append(aiResponse.explanationOfCodeChanges)
        }
        println(output)
        return output



    }


    fun saveGeneratedFiles(sourceFolder: String, sourceFiles: List<SourceFile>) {
        val basePath = "/tmp/ai-repo/${sourceFolder}"
        for (file in sourceFiles) {
            val filePath = "$basePath/${file.sourceFilename.path}/${file.sourceFilename.filename}".replace("//","/")
            println("Saving : $filePath")
            Files.createDirectories(Paths.get("$basePath/${file.sourceFilename.path}"))
            File(filePath).writeText(file.body)
            println("Bestand opgeslagen: $filePath")
        }
    }

}