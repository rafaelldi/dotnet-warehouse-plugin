package me.rafaelldi.dotnet.warehouse.receivingHub

internal data class InboundCargoModel(val version: String, val type: InboundCargoType, val rid: String)

internal enum class InboundCargoType {
    Sdk,
    Runtime,
    AspNetRuntime
}