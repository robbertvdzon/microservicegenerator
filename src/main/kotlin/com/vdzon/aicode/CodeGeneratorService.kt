package com.vdzon.aicode

import org.springframework.ai.ollama.api.OllamaApi
import org.springframework.ai.ollama.api.OllamaApi.ChatRequest
import org.springframework.ai.ollama.api.OllamaApi.ChatResponse
import org.springframework.ai.ollama.api.OllamaApi.Message
import org.springframework.ai.ollama.api.OllamaApi.Message.Role
import org.springframework.ai.ollama.api.OllamaOptions
import org.springframework.stereotype.Service
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vdzon.aicode.model.Request
import com.vdzon.aicode.model.SourceFiles
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

private const val MODEL = "qwen2.5-coder:32b"
//private const val MODEL = "qwen2.5-coder:7b"
//private const val MODEL = "qwen2.5-coder:14b"

@Service
class CodeGeneratorService(
    val githubService: GithubService
) {

    fun generateCode() {
        val ollamaApi = OllamaApi()

        val ollamaOptions = OllamaOptions()
        ollamaOptions.temperature = 0.2
        ollamaOptions.format = "json" // Zorgt ervoor dat JSON wordt gegenereerd.



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
            Generate a JSON object that conforms to the following Kotlin data structure:

            ```kotlin
            data class SourceFiles(val files: List<SourceFile>)
            data class SourceFile(val path: String, val filename: String, val body: String)
            ```
            
            Output **ONLY** valid JSON, nothing else.
            Output all files that are needed to create the project, including the pom.xml
        """.trimIndent()



        val request: ChatRequest = ChatRequest.builder(MODEL)
            .stream(false)
            .messages(
                listOf(
                    Message.builder(Role.SYSTEM)
                        .content(systemPrompt)
                        .build(),
                    Message.builder(Role.USER)
                        .content("""
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
                            
                            """)
                        .build()
                )
            )
            .options(ollamaOptions)
            .build()
        try {
            val response: ChatResponse = ollamaApi.chat(request)

            val jsonResponse = response.message()?.content() ?: "{}"

            println("Ollama JSON output:\n$jsonResponse")

            // JSON parsen naar een SourceFiles-object
            val objectMapper = jacksonObjectMapper()
            val sourceFiles: SourceFiles? = try {
                objectMapper.readValue(jsonResponse)
            } catch (e: Exception) {
                println("Fout bij het parsen van JSON: ${e.message}")
                null
            }

            println("Gekregen SourceFiles object:\n$sourceFiles")
//            if (sourceFiles != null) {
//                saveGeneratedFiles(sourceFiles)
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveGeneratedFiles(sourceFiles: SourceFiles) {
        val basePath = "generated"

        // Zorg ervoor dat de basisfolder bestaat
        Files.createDirectories(Paths.get(basePath))

        for (file in sourceFiles.files) {
            val filePath = "$basePath/${file.path}/${file.filename}"

            // Maak de submap als deze nog niet bestaat
            Files.createDirectories(Paths.get("$basePath/${file.path}"))

            // Schrijf de inhoud naar een bestand
            File(filePath).writeText(file.body)
            println("Bestand opgeslagen: $filePath")
        }
    }
}
