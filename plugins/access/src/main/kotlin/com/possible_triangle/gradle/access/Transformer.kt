package com.possible_triangle.gradle.access

import com.google.gson.GsonBuilder

internal fun ClassTweaker.Modifier.transform(): String =
    when (this) {
        ClassTweaker.Modifier.ACCESSIBLE -> "public"
        ClassTweaker.Modifier.MUTABLE -> "public-f"
        ClassTweaker.Modifier.EXTENDABLE -> "protected-f"
    }

fun String.remapNotation() = replace('/', '.')

fun ClassTweaker.toAccessTransformer(remapper: Remapper = Remapper.empty()): String {
    val transformed =
        entries.filterIsInstance<ClassTweaker.AccessEntry>().map {
            val modifier = it.modifier.transform()
            val className = remapper.remapClass(it.className).remapNotation()

            val additional =
                when (it) {
                    is ClassTweaker.ClassEntry -> emptyList()
                    is ClassTweaker.FieldEntry -> listOf(remapper.remapField(it.className, it.name), "# ${it.name}")
                    is ClassTweaker.MethodEntry -> listOf(remapper.remapMethod(it.className, it.name, it.descriptor), "# ${it.name}")
                }

            listOf(modifier, className) + additional
        }

    val lines = transformed.map { it.joinToString(" ") }
    return lines.joinToString("\n")
}

fun ClassTweaker.toInterfaceInjectionData(remapper: Remapper = Remapper.empty()): String {
    val byClass =
        entries
            .filterIsInstance<ClassTweaker.InjectInterfaceEntry>()
            .groupBy({ remapper.remapClass(it.className) }, { it.interfaceName })

    return GsonBuilder().setPrettyPrinting().create().toJson(byClass)
}
