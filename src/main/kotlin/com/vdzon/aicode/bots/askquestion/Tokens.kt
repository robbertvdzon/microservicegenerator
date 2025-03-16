package com.vdzon.aicode.bots.askquestion

internal class Tokens {

    fun getSystemToken() = """
            You are a very experienced senior Kotlin developer, with a very good knowledge of Kotlin, spring and maven.
            You take your work very serious and you are very precise in your work and try to make the best code possible.
            You will be asked to help to answer questions about code.
            You will be presented the main branch of the project that contains the current code, and also the current specs of application.
            You will also be presented the feature branch that contains the new feature that was added.
            You will be asked to answer questions about the main code, or (when specified) about the feature branch.
            
            You will output the code as a valid JSON output. 
            DO NOT include markdown formatting, explanations, or additional text.       
            DO NOT include the JSON markdown codeblocks like (```json) and (```) in the output.
            Generate a JSON object that conforms to the following Kotlin data structure:

            ```kotlin
            internal data class AiResponse(
                val answer: String
            )            
            ```
            
            Output **ONLY** valid JSON, nothing else.
            You need to respond in the dutch langage
        """.trimIndent()

    fun getUserToken(requestJson: String) = """
                            Can you answer a question about my my project?
                            I will provide the main branch of the project that contains the current code, and also the current specs of application.
                            I will also provide the question than needs to be answered.
                            
                            This will be presented with the following kotling data structure:
                            ```kotlin
                                internal data class Request(
                                    val mainBranch: MicroserviceProject?,
                                    val featureBranch: MicroserviceProject?,
                                    val question: String
                                )
                                data class MicroserviceProject(
                                    val branch: String,
                                    val sourceFiles: List<SourceFile>,
                                    val functionalSpecifications: List<String>,
                                    val technicalSpecifications: List<String>,
                                )
                                
                                data class SourceFile(val path: String, val filename: String, val body: String)
                                
                                data class SourceFiles(val files: List<SourceFile>)
                            ```
                            
                            Here is the json that contains the main branch, the feature branch and the question that needs to be answered:
                            $requestJson
                            """
}