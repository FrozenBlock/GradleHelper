package com.possible_triangle.gradle.access

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.File

private const val TRANSFORM_TASK = "transformAccessWidener"
private const val INTERFACE_INJECTION_TASK = "generateInterfaceInjectionData"

private fun Project.generatedAccessTransformer() = layout.buildDirectory.file("accesstransformer.cfg").map { it.asFile }

private fun Project.generatedInterfaceInjectionData() = layout.buildDirectory.file("interface_injection.json").map { it.asFile }

fun Project.generateAccessTransformer(from: Provider<File>): Pair<Provider<File>, TaskProvider<Task>> {
    val outputFile = generatedAccessTransformer()

    val remapper = detectMappings()
    val transformAccessWidener =
        tasks.register(TRANSFORM_TASK) {
            remapper.configureTask(this)
            outputs.file(outputFile)
            inputs.file(from)

            doLast {
                val classTweaker = parseClassTweaker(from.get())

                classTweaker.entries.filterIsInstance<ClassTweaker.ExtendEnumEntry>().forEach {
                    logger.warn(
                        "Ignoring 'extend-enum' entry for ${it.className}#${it.constantName}: " +
                            "enum extension is only supported on Fabric, not on Forge/NeoForge",
                    )
                }

                val transformed = classTweaker.toAccessTransformer(remapper)
                outputFile.get().writeText(transformed)
            }
        }

    tasks.withType<ProcessResources> {
        dependsOn(transformAccessWidener)
        from(transformAccessWidener) {
            rename { "accesstransformer.cfg" }
            into("META-INF")
        }
    }

    tasks.withType<JavaCompile> {
        dependsOn(transformAccessWidener)
    }

    val output = transformAccessWidener.map { outputFile.get() }

    return output to transformAccessWidener
}

fun Project.generateInterfaceInjectionData(from: Provider<File>): Pair<Provider<File>, TaskProvider<Task>> {
    val outputFile = generatedInterfaceInjectionData()

    val remapper = detectMappings()
    val generateInterfaceInjectionData =
        tasks.register(INTERFACE_INJECTION_TASK) {
            remapper.configureTask(this)
            outputs.file(outputFile)
            inputs.file(from)

            doLast {
                val classTweaker = parseClassTweaker(from.get())
                val transformed = classTweaker.toInterfaceInjectionData(remapper)
                outputFile.get().writeText(transformed)
            }
        }

    val output = generateInterfaceInjectionData.map { outputFile.get() }

    return output to generateInterfaceInjectionData
}

@Suppress("unused")
class AccessWidenerTransformationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<AccessTransformerExtension>("access")
        target.generateAccessTransformer(extension.from.map { it.asFile })
    }
}
