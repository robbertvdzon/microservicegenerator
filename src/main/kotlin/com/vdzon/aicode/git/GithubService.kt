package com.vdzon.aicode.git

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.commonmodel.MicroserviceProject
import com.vdzon.aicode.commonmodel.SourceFile
import com.vdzon.aicode.commonmodel.SourceFileName
import com.vdzon.aicode.commonmodel.Story
import org.eclipse.jgit.api.Git
import java.io.BufferedReader
import java.io.File

//data class GitHubFile(val path: String, val type: String, val url: String?)

class GithubService() {

//    val sessionFactory = object : JschConfigSessionFactory() {
//        override fun configure(host: OpenSshConfig.Host, session: Session) {
//            session.setConfig("StrictHostKeyChecking", "no") // Voorkom host key problemen
//        }
//
//        override fun createDefaultJSch(fs: FS?): JSch {
//            val jsch = super.createDefaultJSch(fs)
//            jsch.addIdentity("/Users/robbertvanderzon/.ssh/id_rsa") // Gebruik je SSH-sleutel
//            return jsch
//        }
//    }

    fun getSerializedRepo(repo: String, branch: String, sourceFolder: String): MicroserviceProject? {
        try {
            return cloneAndListFiles(
                repoUrl = repo,
                branch = branch,
                localPath = "/tmp/ai-repo",
                sourceFolder = sourceFolder
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getTicket(repoUrl: String, ticket: String): Story {
        val repoName = "git@github.com:robbertvdzon/sample-generated-ai-project" // TODO: Deze configureerbaar maken!
        val command = listOf("gh", "issue", "view", ticket, "--repo", repoName, "--json", "title,body")

        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
        process.waitFor()

        if (process.exitValue() != 0) {
            throw RuntimeException("Error fetching ticket: $output")
        }
        val story = jacksonObjectMapper().readValue(output, object : TypeReference<Story>() {})
        return story

    }


    fun cloneAndListFiles(
        repoUrl: String,
        branch: String,
        localPath: String,
        sourceFolder: String
    ): MicroserviceProject {
        cloneRepo(repoUrl, branch, localPath)
        val pathname = "$localPath/$sourceFolder".replace("//", "/")
        val srcDir = File(pathname)

        val sourceFiles = srcDir.walk().filter { it.isFile }.map {
            val path = it.relativeTo(srcDir)

            val path1 = path.parentFile?.path ?: ""
            val filename = path.name
            val body = it.readText()
            SourceFile(
                SourceFileName(path1, filename),
                body
            )
        }.toList()

        val functionalSpecs = File("$localPath/specs/functional_specs.txt")
        val technicalSpecs = File("$localPath/specs/technical_specs.txt")

        return MicroserviceProject(
            branch = branch,
            sourceFiles = sourceFiles,
            functionalSpecifications = if (functionalSpecs.exists()) functionalSpecs.readLines() else emptyList(),
            technicalSpecifications = if (technicalSpecs.exists()) technicalSpecs.readLines() else emptyList(),
        )
    }

    fun cloneRepo(repoUrl: String, branch: String, localPath: String): Git {
        val localDir = File(localPath)
        localDir.deleteRecursively()
        println("Cloning $repoUrl into $localPath...")
        val process = ProcessBuilder("git", "clone","--depth","1", "--branch", branch, repoUrl, localPath)
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().use { it.readText() }
        process.waitFor()
        return Git.open(localDir)
    }

    fun addToGit(git: Git, names: List<SourceFileName>, prefix: String) {
        names.forEach {
            val fullName = "$prefix/${it.path}/${it.filename}".replace("//", "/").removePrefix("/")
            println("adding $fullName")
            git.add().addFilepattern(fullName).call()
        }
    }

    fun removeFromGit(git: Git, names: List<SourceFileName>, prefix: String) {
        names.forEach {
            val fullName = "$prefix/${it.path}/${it.filename}".replace("//", "/").removePrefix("/")
            println("removing $fullName")
            git.rm().addFilepattern(fullName).call()
        }
    }

    fun commit(git: Git, message: String) {
        git.commit().setMessage(message).call()
        println("Committed $message")
    }

    fun push(localDir: String): Boolean {
        val process = ProcessBuilder("git", "push", "origin", "HEAD")
            .directory(File(localDir))
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()
        return exitCode == 0
    }

    fun pushToNewRemoteBranch(localDir: String, branch: String): Boolean {
        val process = ProcessBuilder("git", "push", "--set-upstream", "origin", branch)
            .directory(File(localDir))
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()
        return exitCode == 0
    }

}
