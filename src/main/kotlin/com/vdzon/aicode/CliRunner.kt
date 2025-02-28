//package com.vdzon.aicode
//
//import org.springframework.boot.ApplicationArguments
//import org.springframework.boot.ApplicationRunner
//import org.springframework.stereotype.Component
//
//@Component
//class CliRunner(
//    private val codeGeneratorService: CodeGeneratorService
//) : ApplicationRunner {
//
//    override fun run(args: ApplicationArguments) {
//        if (args.sourceArgs.isEmpty()) {
//            println("Gebruik: java -jar mijnapp.jar generateCode")
////            System.exit(0)
//        }
//
//        val action = args.sourceArgs.joinToString(" ")
//        println("🔹 action: $action")
//
//        codeGeneratorService.generateCode()
//
//        // Stop de applicatie na uitvoeren van de actie
//        System.exit(0)
//    }
//}