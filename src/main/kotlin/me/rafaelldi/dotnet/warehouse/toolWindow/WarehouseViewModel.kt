package me.rafaelldi.dotnet.warehouse.toolWindow

import com.intellij.openapi.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.rafaelldi.dotnet.warehouse.local.LocalDotnetProviderApi
import me.rafaelldi.dotnet.warehouse.local.LocalSdk

internal interface WarehouseViewModelApi : Disposable {
    val localSdkFlow: StateFlow<List<LocalSdk>>
    fun onReloadLocalSdks()
    fun onDeleteSdk(localSdk: LocalSdk)
}

internal class WarehouseViewModel(
    private val viewModelScope: CoroutineScope,
    private val localDotnetProvider: LocalDotnetProviderApi
) : WarehouseViewModelApi {

    private var currentReloadSdksJob: Job? = null

    private val _localSdkFlow = MutableStateFlow(emptyList<LocalSdk>())
    override val localSdkFlow: StateFlow<List<LocalSdk>> = _localSdkFlow.asStateFlow()

    init {
        localDotnetProvider
            .localSdkFlow
            .onEach { _localSdkFlow.emit(it) }
            .launchIn(viewModelScope)
    }

    override fun onReloadLocalSdks() {
        currentReloadSdksJob?.cancel()

        currentReloadSdksJob = viewModelScope.launch {
            localDotnetProvider.reloadLocalSdks()
        }
    }

    override fun onDeleteSdk(localSdk: LocalSdk) {
    }

    override fun dispose() {
        viewModelScope.cancel()
    }
}