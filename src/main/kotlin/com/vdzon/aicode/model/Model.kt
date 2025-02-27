package com.vdzon.aicode.model

data class Request(
    val mainBranch: MicroserviceProject?,
    val featureBranch: MicroserviceProject?,
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