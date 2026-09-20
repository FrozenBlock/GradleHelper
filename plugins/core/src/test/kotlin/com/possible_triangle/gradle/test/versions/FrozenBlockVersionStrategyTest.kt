package com.possible_triangle.gradle.test.versions

import com.possible_triangle.gradle.upload.FrozenBlockVersionStrategy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FrozenBlockVersionStrategyTest {
    private val strategy = FrozenBlockVersionStrategy()

    @Test
    fun `frozenblock fabric release`() {
        val input = TestModVersionProperties(version = "3.0", minecraftVersion = "26.2", loader = "fabric")

        assertEquals("3.0", strategy.modVersion(input))
        assertEquals("3.0-mc26.2", strategy.artifactVersion(input))
        assertEquals("3.0-mc26.2-fabric", strategy.uploadVersion(input))
        assertEquals("3.0", strategy.versionName(input))
        assertEquals("3.0-mc26.2-fabric", strategy.displayName(input))
    }

    @Test
    fun `frozenblock neoforge release`() {
        val input = TestModVersionProperties(version = "3.0", minecraftVersion = "26.2", loader = "neoforge")

        assertEquals("3.0-mc26.2-neoforge", strategy.uploadVersion(input))
        assertEquals("3.0", strategy.versionName(input))
        assertEquals("3.0-mc26.2-neoforge", strategy.displayName(input))
    }

    @Test
    fun `frozenblock unstable`() {
        val input =
            TestModVersionProperties(
                version = "3.0",
                minecraftVersion = "26.2",
                loader = "fabric",
                releaseType = "beta",
            )

        assertEquals("3.0-mc26.2-unstable", strategy.artifactVersion(input))
        assertEquals("3.0-mc26.2-fabric-unstable", strategy.uploadVersion(input))
        assertEquals("3.0-mc26.2-fabric-unstable", strategy.displayName(input))
    }

    @Test
    fun `default strategies keep null display names`() {
        val input = TestModVersionProperties("1.0.0")
        val simple =
            com.possible_triangle.gradle.upload
                .SimpleVersionStrategy()

        assertEquals("1.0.0", simple.uploadVersion(input))
        assertNull(simple.versionName(input))
        assertNull(simple.displayName(input))
    }
}
