@file:OptIn(ExperimentalJewelApi::class, ExperimentalComposeUiApi::class)

package me.rafaelldi.dotnet.warehouse.toolWindow

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.PopupPositionProvider
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.lazy.SelectableLazyColumn
import org.jetbrains.jewel.foundation.lazy.SelectionMode
import org.jetbrains.jewel.foundation.lazy.itemsIndexed
import org.jetbrains.jewel.foundation.lazy.rememberSelectableLazyListState
import org.jetbrains.jewel.ui.component.SimpleListItem
import org.jetbrains.jewel.ui.component.Text

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
    val state = viewModel.warehouseUIState.collectAsState(WarehouseUIState.Empty).value
    if (state is WarehouseUIState.Loading) {
        Box(Modifier.fillMaxSize()) {
            Text("Loading...")
        }
    } else if (state is WarehouseUIState.Success) {
        val listState = rememberSelectableLazyListState()
        SelectableLazyColumn(
            modifier = modifier,
            selectionMode = SelectionMode.Single,
            state = listState,
            onSelectedIndexesChange = { indices ->
                val selectedSdkIndex = indices.firstOrNull() ?: return@SelectableLazyColumn
                viewModel.onSdkSelected(selectedSdkIndex)
            },
        ) {
            itemsIndexed(
                items = state.localSdks,
                key = { _, item -> item },
            ) { index, item ->
                Box(Modifier.wrapContentSize()) {
                    val showPopup = remember { mutableStateOf(false) }
                    val popupPosition = remember { mutableStateOf(IntOffset.Zero) }
                    val itemPosition = remember { mutableStateOf(Offset.Zero) }

                    SimpleListItem(
                        text = "${item.version} - ${item.path}",
                        isSelected = state.selectedIndex == index,
                        isActive = isActive,
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                itemPosition.value = coordinates.positionInWindow()
                            }
                            .onPointerEvent(PointerEventType.Press) { pointerEvent ->
                                if (!pointerEvent.buttons.isSecondaryPressed) return@onPointerEvent

                                val clickOffset = pointerEvent.changes.first().position
                                popupPosition.value = IntOffset(
                                    x = (itemPosition.value.x + clickOffset.x).toInt(),
                                    y = (itemPosition.value.y + clickOffset.y).toInt()
                                )

                                showPopup.value = true
                            }
                    )


                    if (showPopup.value) {
                        val popupPositionProvider = remember(popupPosition.value) {
                            object : PopupPositionProvider {
                                override fun calculatePosition(
                                    anchorBounds: IntRect,
                                    windowSize: IntSize,
                                    layoutDirection: LayoutDirection,
                                    popupContentSize: IntSize
                                ): IntOffset = popupPosition.value
                            }
                        }

                        ContextPopupMenu(
                            popupPositionProvider,
                            onDismissRequest = {
                                showPopup.value = false
                                popupPosition.value = IntOffset.Zero
                                itemPosition.value = Offset.Zero
                            }
                        ) {
                        }
                    }
                }
            }
        }
    }
}