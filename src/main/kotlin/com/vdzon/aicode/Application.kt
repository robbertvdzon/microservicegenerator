package com.vdzon.aicode

import com.vdzon.aicode.bots.AIBots

class Application

fun main(args: Array<String>) {
    val action = args.getOrNull(0)

    AIBots.getAllBots().forEach {
        if (it.getName() == action) {
            try {
                it.run(args)
            } catch (e: Exception) {
                println("Error: ${e.message}")
                println(it.getHelp())
                println()
                e.printStackTrace()
            }
            return
        }
    }
    // no matching bot found
    println("Invalid command, please use one of the following:")
    AIBots.getAllBots().forEach {
        println(it.getHelp())
    }
}



