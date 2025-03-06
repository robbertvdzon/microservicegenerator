package com.vdzon.aicode.model.response

data class AiResponse(
    val modifiedSourceFiles: List<SourceFile>,
    val newSourceFiles: List<SourceFile>,
    val removedSourceFiles: List<SourceFileName>,
    val explanationOfCodeChanges: String,
    val commitMessage: String,
)
