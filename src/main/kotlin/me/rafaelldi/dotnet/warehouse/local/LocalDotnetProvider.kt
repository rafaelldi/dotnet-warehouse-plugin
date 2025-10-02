package me.rafaelldi.dotnet.warehouse.local

internal interface LocalDotnetProvider {
    fun findLocalSdks(): List<LocalSdk>
}