package com.possible_triangle.gradle.test.fabric

import com.possible_triangle.gradle.fabric.FabricExtension
import com.possible_triangle.gradle.fabric.GradleHelperFabricPlugin
import com.possible_triangle.gradle.features.loaders.COMPILED_CLASSES_ELEMENTS
import com.possible_triangle.gradle.features.loaders.LOADER_ATTRIBUTE
import com.possible_triangle.gradle.features.loaders.checkLoaderCompatibility
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.test.createProject
import com.possible_triangle.gradle.test.forceEvaluate
import com.possible_triangle.gradle.test.withProjectDir
import org.gradle.api.GradleException
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FabricTest {
    @Test
    fun `can setup fabric project`() {
        val project =
            createProject<GradleHelperFabricPlugin> {
                withProjectDir("example")
            }

        project.configure<FabricExtension> {
            apiVersion.set("0.76.0+1.19.2")
            loaderVersion.set("0.14.21")
        }

        assertNotNull(project.configurations.getByName("minecraft"))
    }

    @Test
    fun `can customize mod values after fabric block`() {
        val project =
            createProject<GradleHelperFabricPlugin> {
                withProjectDir("example")
            }

        project.configure<FabricExtension> {
            loaderVersion.set("0.14.21")
        }
    }

    @Test
    fun `loader project exposes its compiled classes`() {
        val project =
            createProject<GradleHelperFabricPlugin> {
                withProjectDir("example")
            }.forceEvaluate()

        val compiledClasses = project.configurations.getByName(COMPILED_CLASSES_ELEMENTS)
        assertFalse(compiledClasses.isCanBeResolved)
        assertTrue(compiledClasses.isCanBeConsumed)
        assertEquals("fabric", compiledClasses.attributes.getAttribute(LOADER_ATTRIBUTE))

        val expected =
            project.tasks
                .named<JavaCompile>("compileJava")
                .get()
                .destinationDirectory
                .get()
                .asFile
        assertEquals(expected, compiledClasses.artifacts.single().file)

        assertTrue(compiledClasses.extendsFrom.any { it.name == "api" })
    }

    @Test
    fun `depending on another loader is rejected`() {
        val project =
            createProject<GradleHelperFabricPlugin> {
                withProjectDir("example")
            }

        val failure = assertFailsWith<GradleException> { checkLoaderCompatibility(project, "neoforge") }
        assertTrue(failure.message.orEmpty().contains("'fabric'"))

        checkLoaderCompatibility(project, "fabric")
    }
}
