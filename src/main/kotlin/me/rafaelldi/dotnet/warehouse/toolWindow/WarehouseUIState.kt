package me.rafaelldi.dotnet.warehouse.toolWindow

import me.rafaelldi.dotnet.warehouse.local.LocalSdk

internal class WarehouseUIState(val sdks: List<LocalSdk>) {
    companion object {
        fun empty() = WarehouseUIState(emptyList())
    }
}