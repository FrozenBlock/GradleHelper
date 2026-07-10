package com.possible_triangle.gradle.test.access

import com.possible_triangle.gradle.access.ClassTweaker
import com.possible_triangle.gradle.access.parseClassTweaker
import com.possible_triangle.gradle.access.toAccessTransformer
import com.possible_triangle.gradle.access.toInterfaceInjectionData
import com.possible_triangle.gradle.test.createProjectWithoutPlugin
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AccessWidenerTest {
    @Test
    fun `parses correctly`() {
        val project =
            createProjectWithoutPlugin {
                withProjectDir(File("src/test/resources"))
            }

        val parsed = parseClassTweaker(project.file("aw/simple.accesswidener"))

        assertNotNull(parsed)
    }

    @Test
    fun `transforms correctly`() {
        val project =
            createProjectWithoutPlugin {
                withProjectDir(File("src/test/resources"))
            }

        val parsed = parseClassTweaker(project.file("aw/simple.accesswidener"))
        val transformed = parsed.toAccessTransformer()

        assertNotNull(transformed)
    }

    @Test
    fun `parses class tweaker directives`() {
        val project =
            createProjectWithoutPlugin {
                withProjectDir(File("src/test/resources"))
            }

        val parsed = parseClassTweaker(project.file("aw/simple.classtweaker"))

        val injections = parsed.entries.filterIsInstance<ClassTweaker.InjectInterfaceEntry>()
        assertEquals(2, injections.size)
        assertEquals("com/example/InjectedInterface", injections[0].interfaceName)

        val enumExtensions = parsed.entries.filterIsInstance<ClassTweaker.ExtendEnumEntry>()
        assertEquals(1, enumExtensions.size)
        assertEquals("EXAMPLE_CONSTANT", enumExtensions[0].constantName)
    }

    @Test
    fun `ignores inject-interface and extend-enum when transforming to an access transformer`() {
        val project =
            createProjectWithoutPlugin {
                withProjectDir(File("src/test/resources"))
            }

        val parsed = parseClassTweaker(project.file("aw/simple.classtweaker"))
        val transformed = parsed.toAccessTransformer()

        assertContains(transformed, "com.example.TestClass")
        assertEquals(false, transformed.contains("InjectedInterface"))
        assertEquals(false, transformed.contains("EXAMPLE_CONSTANT"))
    }

    @Test
    fun `transforms inject-interface entries into interface injection data`() {
        val project =
            createProjectWithoutPlugin {
                withProjectDir(File("src/test/resources"))
            }

        val parsed = parseClassTweaker(project.file("aw/simple.classtweaker"))
        val transformed = parsed.toInterfaceInjectionData()

        assertContains(transformed, "com/example/TestClass")
        assertContains(transformed, "com/example/InjectedInterface")
        assertContains(transformed, "com/example/OtherInjectedInterface")
        assertEquals(false, transformed.contains("EXAMPLE_CONSTANT"))
    }
}
