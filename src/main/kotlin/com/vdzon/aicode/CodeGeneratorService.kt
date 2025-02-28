package com.vdzon.aicode

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vdzon.aicode.model.Request
import com.vdzon.aicode.model.SourceFiles
import java.io.BufferedReader
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

//private const val MODEL = "qwen2.5-coder:32b"
private const val MODEL = "qwen2.5-coder:7b"
//private const val MODEL = "qwen2.5-coder:14b"


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
            Generate a JSON object that conforms to the following Kotlin data structure:

            ```kotlin
            data class SourceFiles(val files: List<SourceFile>)
            data class SourceFile(val path: String, val filename: String, val body: String)
            ```
            
            Output **ONLY** valid JSON, nothing else.
            Output all files that are needed to create the project, including the pom.xml
        """.trimIndent()

        val content = """
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

        val jsonSchema = mapOf(
            "type" to "object",
            "properties" to mapOf(
                "files" to mapOf(
                    "type" to "array",
                    "items" to mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "path" to mapOf("type" to "string"),
                            "filename" to mapOf("type" to "string"),
                            "body" to mapOf("type" to "string")
                        ),
                        "required" to listOf("path", "filename", "body")
                    )
                )
            ),
            "required" to listOf("files")
        )

        val request = OllamaRequest(
            model = MODEL,
            messages = listOf(
                Message("system", systemPrompt),
                Message("user", content)
            ),
            format = jsonSchema
        )

        val startTime = System.currentTimeMillis()
        val url = URL("http://localhost:11434/api/chat")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true


        // Verstuur de JSON request
        val requesJson = jacksonObjectMapper().writeValueAsString(request)
        connection.outputStream.use { it.write(requesJson.toByteArray()) }

        // Lees de response als String
        val responseJson = connection.inputStream.bufferedReader().use(BufferedReader::readText)

        val rr = jacksonObjectMapper().readValue<OllamaResponse>(responseJson)


        val content2 = rr?.message?.content ?: ""
        val sourceFiles = jacksonObjectMapper().readValue<SourceFiles>(content2)

        saveGeneratedFiles(sourceFiles)
        val endTime = System.currentTimeMillis()
        println("Tijd: ${endTime - startTime} ms")
    }

    fun saveGeneratedFiles(sourceFiles: SourceFiles) {
        val basePath = "generated"
        Files.createDirectories(Paths.get(basePath))
        for (file in sourceFiles.files) {
            val filePath = "$basePath/${file.path}/${file.filename}"
            Files.createDirectories(Paths.get("$basePath/${file.path}"))
            File(filePath).writeText(file.body)
            println("Bestand opgeslagen: $filePath")
        }
    }
}
