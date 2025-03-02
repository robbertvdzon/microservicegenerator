package com.vdzon.aicode.model

data class AiResponse(
    val modifiedSourceFiles: List<SourceFile>,
    val newSourceFiles: List<SourceFile>,
    val removedSourceFiles: List<SourceFileName>
)
data class SourceFileName(val path: String, val filename: String)
data class SourceFile(val sourceFilename: SourceFileName, val body: String)
