package com.vdzon.aicode.model.response

data class SourceFileName(val path: String, val filename: String)
data class SourceFile(val sourceFilename: SourceFileName, val body: String)
