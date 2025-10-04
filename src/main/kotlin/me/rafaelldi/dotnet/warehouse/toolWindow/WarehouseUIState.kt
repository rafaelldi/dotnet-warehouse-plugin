package me.rafaelldi.dotnet.warehouse.toolWindow

import me.rafaelldi.dotnet.warehouse.local.LocalSdk

internal sealed class WarehouseUIState {
    object Empty : WarehouseUIState()
    object Loading : WarehouseUIState()
    data class Success(val localSdks: List<LocalSdk>, val selectedIndex: Int) : WarehouseUIState()
}