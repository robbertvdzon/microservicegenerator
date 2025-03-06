package com.vdzon.aicode.commonmodel

data class SourceFileName(val path: String, val filename: String)
data class SourceFile(val sourceFilename: SourceFileName, val body: String)
