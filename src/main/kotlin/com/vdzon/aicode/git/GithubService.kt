package com.vdzon.aicode.git

import com.vdzon.aicode.commonmodel.MicroserviceProject
import com.vdzon.aicode.commonmodel.SourceFile
import com.vdzon.aicode.commonmodel.SourceFileName
import org.eclipse.jgit.api.Git
import org.springframework.stereotype.Service
import java.io.File

@Service
class GithubService() {

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
        val process = ProcessBuilder("git", "clone", "--depth", "1", "--branch", branch, repoUrl, localPath)
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
