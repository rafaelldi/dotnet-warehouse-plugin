package me.rafaelldi.dotnet.warehouse.local

import java.nio.file.Path

internal data class LocalSdk(val version: String, val path: Path)
internal data class LocalRuntime(val type: String, val version: String, val path: Path)