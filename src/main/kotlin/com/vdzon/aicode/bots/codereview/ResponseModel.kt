package com.vdzon.aicode.bots.codereview

import com.vdzon.aicode.commonmodel.SourceFileName

internal data class AiResponse(
    val filesWithComments: List<CodeReviewFile>?,
    val generalCodeReviewComments: String
)

data class CodeReviewFile(val sourceFilename: SourceFileName?, val codeReviewComments: String)

