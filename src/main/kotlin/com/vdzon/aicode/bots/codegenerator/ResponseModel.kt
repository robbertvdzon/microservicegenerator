package com.vdzon.aicode.bots.codegenerator

import com.vdzon.aicode.commonmodel.SourceFile
import com.vdzon.aicode.commonmodel.SourceFileName

internal data class AiResponse(
    val modifiedSourceFiles: List<SourceFile>,
    val newSourceFiles: List<SourceFile>,
    val removedSourceFiles: List<SourceFileName>,
    val explanationOfCodeChanges: String,
    val commitMessage: String,
)
