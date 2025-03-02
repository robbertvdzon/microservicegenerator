package com.vdzon.aicode

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.vdzon.aicode.model.MicroserviceProject
import com.vdzon.aicode.model.SourceFile
import com.vdzon.aicode.model.SourceFileName
import com.vdzon.aicode.model.Story
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig
import org.eclipse.jgit.transport.sshd.SshdSessionFactory
import org.eclipse.jgit.util.FS
import java.io.File

data class GitHubFile(val path: String, val type: String, val url: String?)

class GithubService() {

    val sessionFactory = object : JschConfigSessionFactory() {
        override fun configure(host: OpenSshConfig.Host, session: Session) {
            session.setConfig("StrictHostKeyChecking", "no") // Voorkom host key problemen
        }

        override fun createDefaultJSch(fs: FS?): JSch {
            val jsch = super.createDefaultJSch(fs)
            jsch.addIdentity("/Users/robbertvanderzon/.ssh/id_rsa") // Gebruik je SSH-sleutel
            return jsch
        }
    }

    fun getSerializedRepo(branch: String): MicroserviceProject? {
        try {
            return cloneAndListFiles(
                repoUrl = "git@github.com:robbertvdzon/sample-generated-ai-project.git",
                branch = branch,
                localPath = "/tmp/ai-repo"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    fun cloneAndListFiles(repoUrl: String, branch: String, localPath: String): MicroserviceProject {
        cloneRepo(repoUrl, branch, localPath)
        println("📂 Bestanden in branch '$branch':")

        val srcDir = File("$localPath/generated")
        val storiesDir = File("$localPath/stories")

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

        val stories = storiesDir.walk().filter { it.isFile }.map {
            val path = it.relativeTo(srcDir)
            Story(
                path.path,
                it.readText()
            )
        }.toList()

        val functionalSpecs = File("$localPath/specs/functional_specs.txt")
        val technicalSpecs = File("$localPath/specs/technical_specs.txt")

        return MicroserviceProject(
            branch = branch,
            sourceFiles = sourceFiles,
            functionalSpecifications = functionalSpecs.readLines(),
            technicalSpecifications = technicalSpecs.readLines(),
            stories = stories
        )


    }

    val sshSessionFactory = SshdSessionFactory()

    fun cloneRepo(repoUrl: String, branch: String, localPath: String): Git {
        val localDir = File(localPath)
        localDir.deleteRecursively()
        println("Cloning $repoUrl into $localPath...")

        val process = ProcessBuilder("git", "clone", "--branch", branch, repoUrl, localPath)
            .redirectErrorStream(true) // Combineer stdout en stderr
            .start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()

        println(output)

        return Git.open(localDir)
    }
//
//    fun cloneRepo(repoUrl: String, branch: String, localPath: String): Git {
//        val localDir = File(localPath)
//        localDir.deleteRecursively()
//        println("Cloning $repoUrl into $localPath...")
//        val cloneCommand = Git.cloneRepository()
//            .setURI(repoUrl)
//            .setBranch(branch)
//            .setDirectory(localDir)
////        cloneCommand.setTransportConfigCallback(SshTransportConfigCallback(sshSessionFactory))
//
////
////        cloneCommand.setTransportConfigCallback { transport ->
////            if (transport is SshTransport) {
////                transport.sshSessionFactory = sessionFactory
////            }
////        }
//        val git = cloneCommand.call()
//        return git
//    }

    fun addToGit(git: Git, names: List<SourceFileName>, prefix: String) {
        names.forEach {
            println("adding $prefix/${it.path}/${it.filename}")
            git.add().addFilepattern("$prefix/${it.path}/${it.filename}").call()
        }
    }

    fun removeFromGit(git: Git, names: List<SourceFileName>, prefix: String) {
        names.forEach {
            println("removing $prefix/${it.path}/${it.filename}")
            git.rm().addFilepattern("$prefix/${it.path}/${it.filename}").call()
        }
    }

    fun commit(git: Git, message: String) {
        git.commit().setMessage(message).call()
        println("Committed $message")
    }

    fun push(localDir: String): Boolean {
        val process = ProcessBuilder("git", "push", "origin", "HEAD")
            .directory(File(localDir))  // Voer het commando uit in de lokale repo-map
            .redirectErrorStream(true)  // Combineer stdout en stderr
            .start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()

        println(output)

        return exitCode == 0
    }

}
