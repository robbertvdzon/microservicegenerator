package com.vdzon.aicode.bots

interface AIBot {
    fun getName(): String
    fun getDescription(): String
    fun getHelp(): String
    fun run(args: Array<String>)
}