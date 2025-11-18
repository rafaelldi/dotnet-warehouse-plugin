package me.rafaelldi.dotnet.warehouse.local

import java.nio.file.Path
import kotlin.io.path.absolutePathString
import me.rafaelldi.dotnet.warehouse.util.parseSemanticVersion
import me.rafaelldi.dotnet.warehouse.util.extractPreRelease

internal interface DotnetCargo {
    val version: String
    val major: Int
    val minor: Int
    val patch: Int
    val preRelease: String?
    val pathString: String
    val installationType: InstallationType
}

internal data class DotnetSdk(
    override val version: String,
    val path: Path,
    override val installationType: InstallationType
) : DotnetCargo {
    override val pathString: String = path.absolutePathString()

    override val major: Int
    override val minor: Int
    override val patch: Int
    override val preRelease: String?

    init {
        val (maj, min, pat) = parseSemanticVersion(version)
        major = maj
        minor = min
        patch = pat
        preRelease = extractPreRelease(version)
    }
}

internal data class DotnetRuntime(
    val type: String,
    override val version: String,
    val path: Path,
    override val installationType: InstallationType
) : DotnetCargo {
    override val pathString: String = path.absolutePathString()

    override val major: Int
    override val minor: Int
    override val patch: Int
    override val preRelease: String?

    init {
        val (maj, min, pat) = parseSemanticVersion(version)
        major = maj
        minor = min
        patch = pat
        preRelease = extractPreRelease(version)
    }
}