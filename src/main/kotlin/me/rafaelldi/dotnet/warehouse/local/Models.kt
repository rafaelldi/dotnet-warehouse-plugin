package me.rafaelldi.dotnet.warehouse.local

import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal data class LocalSdk(val version: String, val path: Path) {
    val pathString: String = path.absolutePathString()
}
internal data class LocalRuntime(val type: String, val version: String, val path: Path)