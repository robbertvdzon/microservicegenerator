package com.vdzon.aicode.model

data class AiResponse(
    val sourceFiles: SourceFiles
)

data class SourceFile(val path: String, val filename: String, val body: String)

data class SourceFiles(val files: List<SourceFile>)
