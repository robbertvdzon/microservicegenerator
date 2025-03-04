package com.vdzon.aicode

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.aiengine.OllamaEngine
import com.vdzon.aicode.aiengine.OpenAiEngine
import com.vdzon.aicode.model.Request
import com.vdzon.aicode.model.SourceFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

//val aiEngine = OpenAiEngine("gpt-4.5-preview") // beste en snelst, alleen kan instabiel zijn
//val aiEngine = OpenAiEngine("gpt-4o") // beste en snelst en stabiel
val aiEngine = OpenAiEngine("gpt-3.5-turbo") // niet zo goed, wel snel
//val aiEngine = OllamaEngine("qwen2.5-coder:32b")
//val aiEngine = OllamaEngine("qwen2.5-coder:14b")
//val aiEngine = OllamaEngine("qwen2.5-coder:7b")

class CodeGeneratorService(
    val githubService: GithubService
) {

    fun generateCode() {
        val mainCode = githubService.getSerializedRepo("main")
        val branch = githubService.getSerializedRepo("story-002")
        val requestModel = Request(mainCode!!, branch!!)
        val requestJson = jacksonObjectMapper().writeValueAsString(requestModel)

        val systemPrompt = """
            You are a very experienced senior Kotlin developer, with a very good knowledge of Kotlin, spring and maven.
            You take your work very serious and you are very precise in your work and try to make the best code possible.
            You will be asked to help generating new features for an existing project.
            You will be presented the main branch of the project that contains the current code, and also the current specs of application. Also all existing stories are added in the request.
            You will also be presented the feature branch that contains the new feature that needs to be added, including new or modified specifications, and the new story. Also the code that is allready created for this story.
            You will be asked to generate the code for the new feature in the feature branch if that is still missing, or fix any code in the branch that is wrong.
            
            You will output the code as a valid JSON output. 
            DO NOT include markdown formatting, explanations, or additional text.       
            DO NOT include the JSON markdown codeblocks like (```json) and (```) in the output.
            Generate a JSON object that conforms to the following Kotlin data structure:

            ```kotlin
            data class AiResponse(
                val modifiedSourceFiles: List<SourceFile>,
                val newSourceFiles: List<SourceFile>,
                val removedSourceFiles: List<SourceFileName>
            )
            data class SourceFileName(val path: String, val filename: String)
            data class SourceFile(val sourceFilename: SourceFileName, val body: String)
            ```
            
            Output **ONLY** valid JSON, nothing else.
            Output all files that are needed to create the project, including the pom.xml
        """.trimIndent()

        val userPrompt = """
                            I am working on a new feature for my project.
                            I have a json that will show you the main version of the project. This will be the basis of where we will start developing.
                            The json also contains the feature branch that contains the new feature that needs to be added.
                            This will be presented with the following kotling data structure:
                            ```kotlin
                                data class Request(
                                    val mainBranch: MicroserviceProject,
                                    val featureBranch: MicroserviceProject,
                                )
                                
                                data class MicroserviceProject(
                                    val branch: String,
                                    val sourceFiles: List<SourceFile>,
                                    val functionalSpecifications: List<String>,
                                    val technicalSpecifications: List<String>,
                                    val stories: List<Story>,
                                )
                                
                                data class SourceFile(val path: String, val filename: String, val body: String)
                                
                                data class SourceFiles(val files: List<SourceFile>)
                                
                                data class Story(
                                    val storyname: String,
                                    val storyDescription: String
                                )
                            ```
                            
                            Can you help me to generate the code for the new feature and improve the code in the feature branch?
                            Here is the json that contains the main branch and the feature branch:
                            $requestJson
                            """

        val startTime = System.currentTimeMillis()
        val aiResponse = aiEngine.chat(systemPrompt, userPrompt)
        val git = githubService.cloneRepo(
            "git@github.com:robbertvdzon/sample-generated-ai-project.git",
            "story-002",
            "/tmp/ai-repo"
        )
        saveGeneratedFiles(aiResponse.newSourceFiles)
        saveGeneratedFiles(aiResponse.modifiedSourceFiles)
        githubService.addToGit(git, aiResponse.newSourceFiles.map { it.sourceFilename },"generated")
        githubService.addToGit(git, aiResponse.modifiedSourceFiles.map { it.sourceFilename },"generated")
        githubService.removeFromGit(git, aiResponse.removedSourceFiles,"generated")
        githubService.commit(git, "updated by AI")
        githubService.push("/tmp/ai-repo")
        val endTime = System.currentTimeMillis()
        println("Tijd: ${endTime - startTime} ms, ${aiResponse.newSourceFiles.size} new files, ${aiResponse.modifiedSourceFiles.size} modified files, ${aiResponse.removedSourceFiles.size} removed files")
    }

    fun saveGeneratedFiles(sourceFiles: List<SourceFile>) {
        val basePath = "/tmp/ai-repo/generated"
        //        Files.createDirectories(Paths.get("/tmp/ai-repo"))
        for (file in sourceFiles) {
            val filePath = "$basePath/${file.sourceFilename.path}/${file.sourceFilename.filename}"
            Files.createDirectories(Paths.get("$basePath/${file.sourceFilename.path}"))
            File(filePath).writeText(file.body)
            println("Bestand opgeslagen: $filePath")
        }
    }

}
