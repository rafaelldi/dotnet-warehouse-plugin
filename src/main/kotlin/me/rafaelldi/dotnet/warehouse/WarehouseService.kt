package me.rafaelldi.dotnet.warehouse

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level
import com.intellij.platform.util.coroutines.childScope
import kotlinx.coroutines.CoroutineScope

@Service(Level.PROJECT)
internal class WarehouseService(private val scope: CoroutineScope) {
    @Suppress("UnstableApiUsage")
    internal fun createScope(name: String): CoroutineScope = scope.childScope(name)
}