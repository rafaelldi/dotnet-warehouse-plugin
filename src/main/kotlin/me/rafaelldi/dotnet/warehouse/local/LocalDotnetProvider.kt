package me.rafaelldi.dotnet.warehouse.local

internal interface LocalDotnetProvider {
    suspend fun findLocalSdks(): List<LocalSdk>
    suspend fun findLocalRuntimes(): List<LocalRuntime>
}