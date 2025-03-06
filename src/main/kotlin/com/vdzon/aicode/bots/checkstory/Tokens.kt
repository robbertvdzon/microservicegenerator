package com.vdzon.aicode.bots.checkstory

class Tokens {

    fun getSystemToken() = """
            You are a very experienced senior Kotlin developer, with a very good knowledge of Kotlin, spring and maven.
            You take your work very serious and you are very precise in your work and try to make the best code possible.
            
            You will output the code as a valid JSON output. 
            DO NOT include markdown formatting, explanations, or additional text.       
            DO NOT include the JSON markdown codeblocks like (```json) and (```) in the output.
            Generate a JSON object that conforms to the following Kotlin data structure:

            ```kotlin
            data class CheckStoryAiResponse(
                val commentsAboutStory: String,
                val newSuggestedStoryName: String,
                val newSuggestedStoryBody: String,
            )
            ```           
            Output **ONLY** valid JSON, nothing else.
            You respond in the Dutch language.
        """.trimIndent()

    fun getUserToken(requestJson: String) = """
                            I am working on a new feature for my project.
                            I have a json that will show you the main version of the project and the story that needs to be implemented
                            This will be presented with the following kotling data structure:
                            ```kotlin
                                data class CheckStoryRequest(
                                    val mainBranch: MicroserviceProject?,
                                    val storyToImplement: Story
                                )
                                
                                data class MicroserviceProject(
                                    val branch: String,
                                    val sourceFiles: List<SourceFile>,
                                    val functionalSpecifications: List<String>,
                                    val technicalSpecifications: List<String>,
                                )
                                
                                data class SourceFile(val path: String, val filename: String, val body: String)
                                
                                data class SourceFiles(val files: List<SourceFile>)
                                
                                data class Story(
                                    val title: String,
                                    val body: String
                                )
                            ```
                            
                            I would like to know if there is enough information in the story to start implement it.
                            Can you check if the story is clear and complete?
                            Can you respond both with a comment about the story and a new suggested story name and body?

                            Here is the json that contains the main branch and story:
                            $requestJson
                            """
}