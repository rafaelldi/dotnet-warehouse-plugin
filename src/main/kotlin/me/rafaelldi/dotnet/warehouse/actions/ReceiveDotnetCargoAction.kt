package me.rafaelldi.dotnet.warehouse.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.rafaelldi.dotnet.warehouse.WarehouseService
import me.rafaelldi.dotnet.warehouse.receivingHub.ReceivingHub

class ReceiveDotnetCargoAction: AnAction() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return
        val service = ReceivingHub.getInstance()
        val scope = WarehouseService.getInstance(project).createScope(::WarehouseService.name)
        scope.launch(Dispatchers.Default)  {
            service.receiveDotnetReleaseIndex()
        }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}