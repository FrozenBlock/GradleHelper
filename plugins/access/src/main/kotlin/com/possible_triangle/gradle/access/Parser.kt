package com.possible_triangle.gradle.access

import java.io.File

internal fun String.trimComments(): String {
    if (!contains('#')) return this
    return substring(0, indexOf('#'))
}

internal fun parseEntry(statements: List<String>): ClassTweaker.Entry {
    val directive = statements[0].lowercase().removePrefix("transitive-")

    return when (directive) {
        "inject-interface" -> {
            ClassTweaker.InjectInterfaceEntry(statements[1], statements[2])
        }

        "extend-enum" -> {
            ClassTweaker.ExtendEnumEntry(statements[1], statements[2])
        }

        else -> {
            val modifier = ClassTweaker.Modifier.valueOf(directive.uppercase())
            val target = ClassTweaker.Target.valueOf(statements[1].uppercase())
            val className = statements[2]

            when (target) {
                ClassTweaker.Target.CLASS -> ClassTweaker.ClassEntry(modifier, className)
                ClassTweaker.Target.METHOD -> ClassTweaker.MethodEntry(modifier, className, statements[3], statements[4])
                ClassTweaker.Target.FIELD -> ClassTweaker.FieldEntry(modifier, className, statements[3], statements[4])
            }
        }
    }
}

fun parseClassTweaker(file: File): ClassTweaker {
    if (!file.exists()) error("unable to find class tweaker file '$file'")
    val lines =
        file
            .readLines()
            .map { it.trimComments() }
            .map { it.trim() }
            .filterNot { it.isEmpty() }
            .map { it.split("\\s+".toRegex()) }

    val entries = lines.subList(1, lines.size).map(::parseEntry)

    return ClassTweaker(entries)
}
