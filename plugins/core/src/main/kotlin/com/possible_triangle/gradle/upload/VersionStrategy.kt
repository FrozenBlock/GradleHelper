package com.possible_triangle.gradle.upload

import com.possible_triangle.gradle.ModVersionProperties
import com.possible_triangle.gradle.coreProject
import com.possible_triangle.gradle.mod
import com.possible_triangle.gradle.modImpl
import com.possible_triangle.gradle.property
import org.gradle.api.Project
import org.gradle.api.provider.Provider

private fun Project.moduleSuffix(): String {
    if (this == coreProject) return ""
    // frozenblock uses gradle project prefixes to work around gradle composite build usage with multiloader projects
    return "-${projectDir.name.lowercase()}"
}

interface VersionStrategy {
    fun modVersion(mod: ModVersionProperties): String = mod.version.get()

    fun metadataTag(mod: ModVersionProperties): String = modVersion(mod)

    fun artifactName(mod: ModVersionProperties): String = mod.id.get()

    fun artifactVersion(mod: ModVersionProperties): String = modVersion(mod)

    fun uploadVersion(mod: ModVersionProperties): String = modVersion(mod)

    /**
     * Display name for the uploaded file on Modrinth (the `name` field, shown as subtitle).
     * Return null to use the default `Loader Version` format.
     */
    fun versionName(mod: ModVersionProperties): String? = null

    /**
     * Display name for the uploaded file on CurseForge.
     * Return null to default to [versionName] (or the default `Loader Version` format).
     */
    fun displayName(mod: ModVersionProperties): String? = null

    fun baseName(mod: ModVersionProperties): String = "${artifactName(mod)}-${modVersion(mod)}"
}

fun String.toSnapshot(): String =
    if (contains('+')) {
        val version = substringBefore('+')
        val metadata = substringAfter('+')
        "${version.toSnapshot()}+$metadata"
    } else if (contains('-')) {
        "$this.SNAPSHOT"
    } else {
        "$this-SNAPSHOT"
    }

fun String.stripVersionMetadata() = substringBeforeLast('+')

fun String.addVersionMetadata(part: String): String =
    if (contains('+')) {
        val parts = substringAfterLast('+')
        if (parts.contains(part)) {
            this
        } else {
            plus(".$part")
        }
    } else {
        plus("+$part")
    }

class PassthroughVersionStrategy : VersionStrategy

open class SimpleVersionStrategy : VersionStrategy {
    override fun metadataTag(mod: ModVersionProperties): String = super.modVersion(mod)

    override fun modVersion(mod: ModVersionProperties): String = super.modVersion(mod).stripVersionMetadata()
}

class WithMinecraftVersion(
    private val inner: VersionStrategy,
) : VersionStrategy {
    override fun modVersion(mod: ModVersionProperties): String = inner.modVersion(mod)

    override fun metadataTag(mod: ModVersionProperties): String =
        inner.metadataTag(mod).addVersionMetadata("mc${mod.minecraftVersion.get()}")

    override fun artifactName(mod: ModVersionProperties): String = "${inner.artifactName(mod)}-${mod.minecraftVersion.get()}"

    override fun baseName(mod: ModVersionProperties) = inner.baseName(mod)
}

class WithLoader(
    private val inner: VersionStrategy,
) : VersionStrategy {
    private val ModVersionProperties.loaderName
        get() =
            loader.orNull ?: error("cannot only WithLoader strategy in a loader project")

    override fun modVersion(mod: ModVersionProperties): String = inner.modVersion(mod)

    override fun metadataTag(mod: ModVersionProperties): String = inner.metadataTag(mod).addVersionMetadata(mod.loaderName)

    override fun artifactName(mod: ModVersionProperties): String = "${inner.artifactName(mod)}-${mod.loaderName}"
}

class FrozenBlockVersionStrategy : VersionStrategy {
    override fun artifactVersion(mod: ModVersionProperties): String {
        var version = "${modVersion(mod)}-mc${mod.minecraftVersion.get()}"

        if (mod.releaseType.get() != "release") {
            version += "-unstable"
        }

        return version
    }

    override fun uploadVersion(mod: ModVersionProperties): String {
        val loader = mod.loader.orNull?.takeUnless { it.isBlank() }
        var version = "${modVersion(mod)}-mc${mod.minecraftVersion.get()}"

        if (!loader.isNullOrBlank()) {
            version += "-$loader"
        }

        if (mod.releaseType.get() != "release") {
            version += "-unstable"
        }

        return version
    }

    override fun versionName(mod: ModVersionProperties): String = modVersion(mod)

    override fun displayName(mod: ModVersionProperties): String = uploadVersion(mod)

    override fun baseName(mod: ModVersionProperties): String = "${artifactName(mod)}-${artifactVersion(mod)}"
}

internal fun parseVersionStrategy(id: String): VersionStrategy =
    when (id.lowercase()) {
        "simple" -> SimpleVersionStrategy()
        "suffix_minecraft_version" -> WithMinecraftVersion(SimpleVersionStrategy())
        "with_minecraft_version" -> WithMinecraftVersion(SimpleVersionStrategy())
        "with_loader" -> WithLoader(SimpleVersionStrategy())
        "frozenblock" -> FrozenBlockVersionStrategy()
        else -> error("unknown version strategy '$id'")
    }

internal fun Project.metadataTagConvention(): Provider<String> = mod.versionStrategy.map { it.metadataTag(modImpl) }

internal fun Project.artifactVersionConvention(): Provider<String> = mod.versionStrategy.map { it.artifactVersion(modImpl) }

internal fun Project.uploadVersionConvention(): Provider<String> = mod.versionStrategy.map { it.uploadVersion(modImpl) }

internal fun Project.strategyVersionNameConvention(): Provider<String> =
    mod.versionStrategy.flatMap { strategy ->
        objects.property(strategy.versionName(modImpl))
    }

internal fun Project.strategyDisplayNameConvention(): Provider<String> =
    mod.versionStrategy.flatMap { strategy ->
        objects.property(strategy.displayName(modImpl))
    }

internal fun Project.artifactNameConvention(): Provider<String> =
    mod.versionStrategy.map {
        val suffix = moduleSuffix()
        it.artifactName(modImpl) + suffix
    }

internal fun Project.baseNameConvention(): Provider<String> =
    mod.versionStrategy.map {
        val suffix = moduleSuffix()
        it.baseName(modImpl) + suffix
    }
