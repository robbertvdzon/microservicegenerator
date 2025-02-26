package com.vdzon.aicode

import jakarta.annotation.PostConstruct
import org.springframework.ai.ollama.api.OllamaApi
import org.springframework.ai.ollama.api.OllamaApi.ChatRequest
import org.springframework.ai.ollama.api.OllamaApi.ChatResponse
import org.springframework.ai.ollama.api.OllamaApi.Message
import org.springframework.ai.ollama.api.OllamaApi.Message.Role
import org.springframework.ai.ollama.api.OllamaOptions
import org.springframework.stereotype.Service
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

data class SourceFiles(val files: List<SourceFile>)
data class SourceFile(val path: String, val filename: String, val body: String)

@Service
class MyService() {

    @PostConstruct
    fun askOllamaOnStartup() {
        val ollamaApi = OllamaApi()

        val ollamaOptions = OllamaOptions()
        ollamaOptions.temperature = 0.2
        ollamaOptions.format = "json" // Zorgt ervoor dat JSON wordt gegenereerd.

        val prompt = """
            You are an AI assistant that generates valid JSON output. 
            DO NOT include markdown formatting, explanations, or additional text.
            
            Generate a JSON object that conforms to the following Kotlin data structure:

            ```kotlin
            data class SourceFiles(val files: List<SourceFile>)
            data class SourceFile(val path: String, val filename: String, val body: String)
            ```

            The JSON must include a valid Kotlin file with a Spring Boot REST controller.
            Output **ONLY** valid JSON, nothing else.
            Output all files that are needed to create the project, including the pom.xml
        """.trimIndent()

        val request: ChatRequest = ChatRequest.builder("qwen2.5-coder:32b")
//        val request: ChatRequest = ChatRequest.builder("codellama:instruct")
            .stream(false)
            .messages(
                listOf(
                    Message.builder(Role.SYSTEM)
                        .content(prompt)
                        .build(),
                    Message.builder(Role.USER)
                        .content("Create a Spring Kotlin project with an endpoint that returns 'Hello World'.  Include a testclass for this project.")
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
            if (sourceFiles != null) {
                saveGeneratedFiles(sourceFiles)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveGeneratedFiles(sourceFiles: SourceFiles) {
        val basePath = "/Users/robbertvdzon/git/aiCodeGenerator1/generated"

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
