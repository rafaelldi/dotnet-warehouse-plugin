package me.rafaelldi.dotnet.warehouse.toolWindow

import com.intellij.openapi.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.rafaelldi.dotnet.warehouse.local.DotnetArtifactProviderApi
import me.rafaelldi.dotnet.warehouse.local.DotnetSdk

internal interface WarehouseViewModelApi : Disposable {
    val dotnetSdkFlow: StateFlow<List<DotnetSdk>>
    fun onReloadLocalSdks()
    fun onDeleteSdk(dotnetSdk: DotnetSdk)
}

internal class WarehouseViewModel(
    private val viewModelScope: CoroutineScope,
    private val dotnetArtifactProvider: DotnetArtifactProviderApi
) : WarehouseViewModelApi {

    private var currentReloadSdksJob: Job? = null

    private val _dotnetSdkFlow = MutableStateFlow(emptyList<DotnetSdk>())
    override val dotnetSdkFlow: StateFlow<List<DotnetSdk>> = _dotnetSdkFlow.asStateFlow()

    init {
        dotnetArtifactProvider
            .dotnetSdkFlow
            .onEach { _dotnetSdkFlow.emit(it) }
            .launchIn(viewModelScope)
    }

    override fun onReloadLocalSdks() {
        currentReloadSdksJob?.cancel()

        currentReloadSdksJob = viewModelScope.launch {
            dotnetArtifactProvider.reloadDotnetSdks()
        }
    }

    override fun onDeleteSdk(dotnetSdk: DotnetSdk) {
    }

    override fun dispose() {
        viewModelScope.cancel()
    }
}