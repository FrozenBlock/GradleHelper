package com.possible_triangle.gradle.settings

import org.gradle.api.initialization.Settings
import java.io.File

private val onCI get() = System.getenv("GITHUB_ACTIONS") == "true"

private fun localCheckoutPath(repo: String) = if (onCI) repo else "../$repo"

/**
 * Substitutes a published dependency (e.g. "net.frozenblock:frozenlib") with a sibling checkout
 * of that mod's build, if one exists (e.g. "../FrozenLib"). [prefix] is the other mod's Gradle
 * project name prefix (e.g. "flib" for ":flib-fabric"). Set [candlelight] when depending on a mod
 * that uses the candlelight annotation processor.
 */
fun Settings.localRepository(
    repo: String,
    dependencySub: String,
    prefix: String = "",
    multi: Boolean = true,
    candlelight: Boolean = false,
    suffixes: List<String> = listOf("common", "fabric", "neoforge"),
    enabled: Boolean = true,
) {
    if (!enabled) return

    val path = localCheckoutPath(repo)
    val file = File(path)
    if (!file.exists()) {
        println("Local repo $repo not found at $path")
        return
    }

    includeBuild(path) {
        dependencySubstitution {
            if (multi && suffixes.isNotEmpty()) {
                for (suffix in suffixes) {
                    substitute(module("$dependencySub-$suffix")).using(project(":$prefix-$suffix"))
                }
            } else {
                val projectPath =
                    if (dependencySub.isNotEmpty()) {
                        if (prefix.isNotEmpty()) ":$prefix-$dependencySub" else ":$dependencySub"
                    } else {
                        ":"
                    }
                substitute(module(dependencySub)).using(project(projectPath))
            }
        }
    }

    if (multi && candlelight) {
        gradle.rootProject {
            subprojects {
                val suffix = suffixes.find { project.name.endsWith("-$it") }
                if (suffix != null) {
                    afterEvaluate {
                        tasks.findByName("compileJava")?.dependsOn(
                            gradle.includedBuild(repo).task(":$prefix-$suffix:candleLightTransform"),
                        )
                    }
                }
            }
        }
    }

    println("Included local repo $repo")
}
