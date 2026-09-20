package com.possible_triangle.gradle.test

import com.modrinth.minotaur.TaskModrinthUpload
import com.possible_triangle.gradle.GradleHelperCorePlugin
import com.possible_triangle.gradle.upload.UploadExtension
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UploadTest {
    @Test
    fun `can configure cursegradle without token`() {
        val project =
            createProject<GradleHelperCorePlugin> {
                withProjectDir("example")
            }

        project.the<UploadExtension>().curseforge {
            projectId.set("test-id")
        }
    }

    @Test
    fun `can configure modrinth without token`() {
        val project =
            createProject<GradleHelperCorePlugin> {
                withProjectDir("example")
            }

        project.the<UploadExtension>().modrinth {
            projectId.set("test-id")
        }
    }

    private fun createConfiguredProject(): org.gradle.api.Project {
        val project =
            createProject<GradleHelperCorePlugin> {
                withProjectDir("example")
            }

        val jar = project.tasks.getByName<Jar>("jar")
        project.the<UploadExtension>().forEach {
            projectId.set("12345")
            token.set("test-token")
            file.set(jar.archiveFile)
            changelog.set("test changelog")
        }

        return project
    }

    @Test
    fun `curseforge upload resolves jar output lazily`() {
        val project = createConfiguredProject()
        project.forceEvaluate()

        val task = project.tasks.getByName<TaskPublishCurseForge>("curseforge")
        val artifact = task.uploadArtifacts.single()

        project.tasks
            .getByName<Jar>("jar")
            .archiveFileName
            .set("changed.jar")
        assertEquals("changed.jar", artifact.artifact.singleFile.name)
    }

    @Test
    fun `upload tasks depend on shadowJar when present`() {
        val project = createConfiguredProject()
        project.tasks.register<Jar>("shadowJar")
        project.forceEvaluate()

        val shadow = project.tasks.getByName("shadowJar")
        listOf("curseforge", "modrinth").forEach { name ->
            val deps =
                project.tasks
                    .getByName(name)
                    .taskDependencies
                    .getDependencies(project.tasks.getByName(name))
            assertTrue(deps.contains(shadow), "$name should depend on shadowJar")
        }
    }

    @Test
    fun `upload tasks work without shadowJar`() {
        val project = createConfiguredProject()
        project.forceEvaluate()

        project.tasks.getByName<TaskPublishCurseForge>("curseforge")
        project.tasks.getByName<TaskModrinthUpload>("modrinth")
    }
}
