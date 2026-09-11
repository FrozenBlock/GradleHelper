package com.possible_triangle.gradle.test.common

import com.possible_triangle.gradle.common.GradleHelperCommonPlugin
import com.possible_triangle.gradle.features.loaders.COMPILED_CLASSES_ELEMENTS
import com.possible_triangle.gradle.features.loaders.LOADER_ATTRIBUTE
import com.possible_triangle.gradle.features.loaders.checkLoaderCompatibility
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.test.createProject
import com.possible_triangle.gradle.test.forceEvaluate
import com.possible_triangle.gradle.test.withProjectDir
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.named
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommonTest {
    @Test
    fun `can setup common project`() {
        val project =
            createProject<GradleHelperCommonPlugin> {
                withProjectDir("example")
            }

        project.mod {
            minecraftVersion.set("1.19.2")
        }
    }

    @Test
    fun `can customize mod values after common block`() {
        val project =
            createProject<GradleHelperCommonPlugin> {
                withProjectDir("example")
            }

        project.mod {
            minecraftVersion.set("1.19.2")
        }
    }

    @Test
    fun `common project exposes its compiled classes`() {
        val project =
            createProject<GradleHelperCommonPlugin> {
                withProjectDir("example")
            }.forceEvaluate()

        val compiledClasses = project.configurations.getByName(COMPILED_CLASSES_ELEMENTS)
        assertFalse(compiledClasses.isCanBeResolved)
        assertTrue(compiledClasses.isCanBeConsumed)
        assertEquals("common", compiledClasses.attributes.getAttribute(LOADER_ATTRIBUTE))

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
    fun `common projects can be depended on from any loader`() {
        val project =
            createProject<GradleHelperCommonPlugin> {
                withProjectDir("example")
            }

        checkLoaderCompatibility(project, "fabric")
        checkLoaderCompatibility(project, "neoforge")
    }
}
