package me.rafaelldi.dotnet.warehouse.local

import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal interface DotnetArtifact {
    val version: String
    val pathString: String
}

internal data class DotnetSdk(
    override val version: String,
    val path: Path
) : DotnetArtifact {
    override val pathString: String = path.absolutePathString()
}

internal data class DotnetRuntime(
    val type: String,
    override val version: String,
    val path: Path
) : DotnetArtifact {
    override val pathString: String = path.absolutePathString()
}