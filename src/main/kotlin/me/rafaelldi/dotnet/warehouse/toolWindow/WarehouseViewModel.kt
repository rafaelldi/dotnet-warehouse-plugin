package me.rafaelldi.dotnet.warehouse.toolWindow

import com.intellij.openapi.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.rafaelldi.dotnet.warehouse.local.LocalDotnetProvider

internal class WarehouseViewModel(
    private val viewModelScope: CoroutineScope,
    private val localSdkProvider: LocalDotnetProvider
) : Disposable {

    private var currentWarehouseJob: Job? = null

    private val _warehouseState = MutableStateFlow<WarehouseUIState>(WarehouseUIState.Empty)
    internal val warehouseUIState: Flow<WarehouseUIState> = _warehouseState.asStateFlow()

    internal fun onReloadWarehouse() {
        currentWarehouseJob?.cancel()

        currentWarehouseJob = viewModelScope.launch {
            val sdks = localSdkProvider.findLocalSdks()
            _warehouseState.value = WarehouseUIState.Success(sdks, 0)
        }
    }

    internal fun onSdkSelected(index: Int) {
        val state = _warehouseState.value
        if (state !is WarehouseUIState.Success) return

        val newState = state.copy(selectedIndex = index)
        _warehouseState.value = newState
    }

    override fun dispose() {
        viewModelScope.cancel()
    }
}