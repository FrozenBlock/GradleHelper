package com.possible_triangle.gradle.access

/**
 * credits to [isXander/modstitch](https://github.com/isXander/modstitch/blob/master/src/main/kotlin/dev/isxander/modstitch/util/AccessWidener.kt)
 */
data class ClassTweaker(
    val entries: List<Entry>,
) {
    sealed interface Entry {
        val className: String
    }

    sealed interface AccessEntry : Entry {
        val target: Target
        val modifier: Modifier
    }

    data class ClassEntry(
        override val modifier: Modifier,
        override val className: String,
    ) : AccessEntry {
        override val target = Target.CLASS
    }

    data class MethodEntry(
        override val modifier: Modifier,
        override val className: String,
        val name: String,
        val descriptor: String,
    ) : AccessEntry {
        override val target = Target.METHOD
    }

    data class FieldEntry(
        override val modifier: Modifier,
        override val className: String,
        val name: String,
        val descriptor: String,
    ) : AccessEntry {
        override val target = Target.FIELD
    }

    data class InjectInterfaceEntry(
        override val className: String,
        val interfaceName: String,
    ) : Entry

    data class ExtendEnumEntry(
        override val className: String,
        val constantName: String,
    ) : Entry

    enum class Modifier {
        ACCESSIBLE,
        MUTABLE,
        EXTENDABLE,
    }

    enum class Target {
        CLASS,
        METHOD,
        FIELD,
    }
}
