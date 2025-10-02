package me.rafaelldi.dotnet.warehouse.toolWindow

import androidx.compose.runtime.LaunchedEffect
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import me.rafaelldi.dotnet.warehouse.WarehouseService
import me.rafaelldi.dotnet.warehouse.local.LocalDotnetService
import org.jetbrains.jewel.bridge.addComposeTab


internal class WarehouseToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val viewModel = WarehouseViewModel(
            project.service<WarehouseService>().createScope(::WarehouseViewModel.name),
            LocalDotnetService.getInstance(project)
        )
        Disposer.register(toolWindow.disposable, viewModel)

        toolWindow.addComposeTab("SDKs") {
            LaunchedEffect(Unit) {
                viewModel.onReloadWarehouse()
            }
            WarehouseTab(viewModel)
        }
    }
}
