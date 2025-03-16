package com.vdzon.aicode.bots.createbranch

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.GithubService
import com.vdzon.aicode.aiengine.AiEngineFactory
import com.vdzon.aicode.aiengine.util.JsonSchemaHelper
import com.vdzon.aicode.bots.AIBot
import com.vdzon.aicode.commonmodel.SourceFile
import org.eclipse.jgit.api.Git
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class CreateBranchBot(
): AIBot {
    override fun getName(): String = "create_branch"
    override fun getDescription(): String = "Implement a story"
    override fun getHelp(): String = "create_branch githubrepo mainbranch featurebranch story engine model"
    override fun run(args: Array<String>): String{
        val repo = args.getOrNull(1) ?: throw RuntimeException("Invalid repo")
        val mainbranch = args.getOrNull(2) ?: throw RuntimeException("Invalid main branch")
        val featurebranch = args.getOrNull(3) ?: throw RuntimeException("feature branch")
        val story = args.getOrNull(4) ?: throw RuntimeException("Invalid story")
        val engine = args.getOrNull(5) ?: throw RuntimeException("Invalid engine")
        val model = args.getOrNull(6) ?: throw RuntimeException("Invalid model")

        val tokenGenerator = Tokens()
        val aiEngine= AiEngineFactory.getAiEngine(engine, model)
        val githubService =  GithubService()

        println("\nStart generating code..")
        val storyToImplement = githubService.getTicket(repo, story)
        val mainCode = githubService.getSerializedRepo(mainbranch)
        println("calling AI model..")

        val startTime = System.currentTimeMillis()
        val requestModel = Request(mainCode!!,storyToImplement)
        val requestJson = jacksonObjectMapper().writeValueAsString(requestModel)
        val jsonSchema: Map<String, Any> = JsonSchemaHelper.generateJsonSchemaAsMap(AiResponse::class.java)
        val jsonResponse = aiEngine.chat(jsonSchema, tokenGenerator.getSystemToken(), tokenGenerator.getUserToken(requestJson))
        val aiResponse: AiResponse = jacksonObjectMapper().readValue(jsonResponse,object : TypeReference<AiResponse>() {})
        val endTime = System.currentTimeMillis()

        println("Ai finished in: ${endTime - startTime} ms, ${aiResponse.newSourceFiles.size} new files, ${aiResponse.modifiedSourceFiles.size} modified files, ${aiResponse.removedSourceFiles.size} removed files")

        val git: Git = githubService.cloneRepo(
            repo,
            mainbranch,
            "/tmp/ai-repo"
        )
        println("Create branch..")
        git.branchCreate().setName(featurebranch).call()
        git.checkout().setName(featurebranch).call()


        println("Saving and pushing generated files..")
        saveGeneratedFiles(aiResponse.newSourceFiles)
        saveGeneratedFiles(aiResponse.modifiedSourceFiles)
        githubService.addToGit(git, aiResponse.newSourceFiles.map { it.sourceFilename },"generated")
        githubService.addToGit(git, aiResponse.modifiedSourceFiles.map { it.sourceFilename },"generated")
        githubService.removeFromGit(git, aiResponse.removedSourceFiles,"generated")
        githubService.commit(git, aiResponse.commitMessage)
        githubService.pushToNewRemoteBranch("/tmp/ai-repo", featurebranch)
        println("\nExplanation from AI:")
        print(aiResponse.explanationOfCodeChanges)

        return "DONE.."

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