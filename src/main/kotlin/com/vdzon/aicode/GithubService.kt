package com.vdzon.aicode

import com.vdzon.aicode.model.MicroserviceProject
import com.vdzon.aicode.model.SourceFile
import com.vdzon.aicode.model.Story
import org.eclipse.jgit.api.Git
import java.io.File

data class GitHubFile(val path: String, val type: String, val url: String?)

class GithubService() {

    fun getSerializedRepo(branch: String): MicroserviceProject? {
        try {
            return cloneAndListFiles(
                repoUrl = "https://github.com/robbertvdzon/sample-generated-ai-project.git",
                branch = branch,
                localPath = "/tmp/ai-repo"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    fun cloneAndListFiles(repoUrl: String, branch: String, localPath: String): MicroserviceProject {
        val localDir = File(localPath)
        localDir.deleteRecursively()
        if (localDir.exists()) {
            println("Repo bestaat al lokaal: $localPath")
        } else {
            println("Cloning $repoUrl into $localPath...")
            val git = Git.cloneRepository()
                .setURI(repoUrl)
                .setBranch(branch)
                .setDirectory(localDir)
                .call()
        }

        println("📂 Bestanden in branch '$branch':")

        val srcDir = File("$localPath/generated")
        val storiesDir = File("$localPath/stories")

        val sourceFiles = srcDir.walk().filter { it.isFile }.map {
            val path = it.relativeTo(srcDir)

            val path1 = path.parentFile?.path ?: ""
            val filename = path.name
            val body = it.readText()
            SourceFile(
                path1,
                filename,
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
}
