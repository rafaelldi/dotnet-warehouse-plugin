@file:OptIn(ExperimentalJewelApi::class)

package me.rafaelldi.dotnet.warehouse.toolWindow

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.lazy.SelectableLazyColumn
import org.jetbrains.jewel.foundation.lazy.SelectionMode
import org.jetbrains.jewel.foundation.lazy.itemsIndexed
import org.jetbrains.jewel.ui.component.SimpleListItem

@Composable
internal fun WarehouseTab(viewModel: WarehouseViewModel) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        LocalSdkList(viewModel, Modifier.fillMaxSize())
    }
}

@Composable
private fun LocalSdkList(
    viewModel: WarehouseViewModel,
    modifier: Modifier
) {
    val state = viewModel.warehouseUIState.collectAsState(WarehouseUIState.empty()).value

    SelectableLazyColumn(
        modifier = modifier,
        selectionMode = SelectionMode.Single
    ) {
        itemsIndexed(
            items = state.sdks,
            key = { _, item -> item.path },
        ) { index, item ->
            Box(Modifier.wrapContentSize()) {
                SimpleListItem(
                    text = item.path.toString(),
                    isSelected = false
                )
            }
        }
    }
}