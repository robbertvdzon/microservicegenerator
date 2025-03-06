package com.vdzon.aicode.token


class CodeGeneratorTokens {

    fun getSystemToken() = """
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
                val removedSourceFiles: List<SourceFileName>,
                val explanationOfCodeChanges: String,
                val commitMessage: String,
            )
            data class SourceFileName(val path: String, val filename: String)
            data class SourceFile(val sourceFilename: SourceFileName, val body: String)
            ```
            
            Output **ONLY** valid JSON, nothing else.
            Output all files that are needed to create the project, including the pom.xml
        """.trimIndent()

    fun getUserToken(requestJson: String) = """
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
}