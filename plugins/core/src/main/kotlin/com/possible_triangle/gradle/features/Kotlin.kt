package com.possible_triangle.gradle.features

import com.possible_triangle.gradle.features.loaders.mainSourceSet
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool

fun Project.enableKotlin() =
    allprojects {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

        disableKotlinBuildToolsApi()

        mainSourceSet.java.srcDir("src/main/kotlin")
    }

fun Project.detectKotlin(): Boolean = plugins.findPlugin("org.jetbrains.kotlin.jvm") != null

fun Project.disableKotlinBuildToolsApi() {
    if ((findProperty("kotlin.compiler.runViaBuildToolsApi") as? String)?.equals("true", ignoreCase = true) == true) return
    tasks.withType(AbstractKotlinCompileTool::class.java).configureEach {
        try {
            @Suppress("UNCHECKED_CAST")
            val runViaBta = javaClass.getMethod($$"getRunViaBuildToolsApi$kotlin_gradle_plugin_common")
                .invoke(this) as? Property<Boolean>
            runViaBta?.set(false)
        } catch (_: ReflectiveOperationException) {
        }
    }
}
