package com.vdzon.aicode

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.aiengine.OpenAiEngine
import com.vdzon.aicode.model.AiResponse
import com.vdzon.aicode.model.Request
import com.vdzon.aicode.model.SourceFile
import com.vdzon.aicode.token.CodeGeneratorTokens
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

val aiEngine = OpenAiEngine("gpt-4.5-preview") // beste en snelst, alleen kan instabiel zijn
//val aiEngine = OpenAiEngine("gpt-4o") // beste en snelst en stabiel
//val aiEngine = OpenAiEngine("gpt-3.5-turbo") // niet zo goed, wel snel
//val aiEngine = OllamaEngine("qwen2.5-coder:32b")
//val aiEngine = OllamaEngine("qwen2.5-coder:14b")
//val aiEngine = OllamaEngine("qwen2.5-coder:7b")

class CodeGeneratorService(
    val githubService: GithubService,
    val repo: String,
    val mainbranch: String,
    val featurebranch: String,
    val story: String,
    val engine: String,
    val model: String
) {
    private val tokenGenerator = CodeGeneratorTokens()

    fun generateCode() {
        println("\nStart generating code..")
        val mainCode = githubService.getSerializedRepo(mainbranch)
        val branch = githubService.getSerializedRepo(featurebranch)
        println("calling AI model..")

        val startTime = System.currentTimeMillis()
        val requestModel = Request(mainCode!!, branch!!)
        val requestJson = jacksonObjectMapper().writeValueAsString(requestModel)
        val aiResponse: AiResponse = aiEngine.chat(tokenGenerator.getSystemToken(), tokenGenerator.getUserToken(requestJson))
        val endTime = System.currentTimeMillis()

        println("Ai finished in: ${endTime - startTime} ms, ${aiResponse.newSourceFiles.size} new files, ${aiResponse.modifiedSourceFiles.size} modified files, ${aiResponse.removedSourceFiles.size} removed files")

        val git = githubService.cloneRepo(
            repo,
            featurebranch,
            "/tmp/ai-repo"
        )
        println("Saving and pushing generated files..")
        saveGeneratedFiles(aiResponse.newSourceFiles)
        saveGeneratedFiles(aiResponse.modifiedSourceFiles)
        githubService.addToGit(git, aiResponse.newSourceFiles.map { it.sourceFilename },"generated")
        githubService.addToGit(git, aiResponse.modifiedSourceFiles.map { it.sourceFilename },"generated")
        githubService.removeFromGit(git, aiResponse.removedSourceFiles,"generated")
        githubService.commit(git, "updated by AI")
//        githubService.push("/tmp/ai-repo")
        println("\nExplanation from AI:")
        print(aiResponse.explanationOfCodeChanges)
    }

    fun saveGeneratedFiles(sourceFiles: List<SourceFile>) {
        val basePath = "/tmp/ai-repo/generated"
        for (file in sourceFiles) {
            val filePath = "$basePath/${file.sourceFilename.path}/${file.sourceFilename.filename}"
            Files.createDirectories(Paths.get("$basePath/${file.sourceFilename.path}"))
            File(filePath).writeText(file.body)
            println("Bestand opgeslagen: $filePath")
        }
    }

}
