package me.rafaelldi.dotnet.warehouse.local

import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal data class DotnetSdk(val version: String, val path: Path) {
    val pathString: String = path.absolutePathString()
}
internal data class DotnetRuntime(val type: String, val version: String, val path: Path)