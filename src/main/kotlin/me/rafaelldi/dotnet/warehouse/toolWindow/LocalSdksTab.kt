@file:OptIn(ExperimentalJewelApi::class, ExperimentalComposeUiApi::class)

package me.rafaelldi.dotnet.warehouse.toolWindow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.PopupPositionProvider
import me.rafaelldi.dotnet.warehouse.WarehouseBundle
import me.rafaelldi.dotnet.warehouse.local.DotnetSdk
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.colorPalette
import kotlin.io.path.absolutePathString

@Composable
internal fun DotnetSdksTab(viewModel: WarehouseViewModelApi) {
    val localSdks by viewModel.dotnetSdkFlow.collectAsState(emptyList())

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.onReloadLocalSdks()
    }

    Column(
        Modifier
            .fillMaxWidth()
    ) {
        DotnetSdkList(
            localSdks,
            listState,
            viewModel,
            Modifier
                .fillMaxSize()
        )
    }
}

@Composable
private fun DotnetSdkList(
    dotnetSdks: List<DotnetSdk>,
    listState: LazyListState,
    viewModel: WarehouseViewModelApi,
    modifier: Modifier
) {
    Box(modifier = modifier) {
        if (dotnetSdks.isEmpty()) {
            EmptySdkListPlaceholder()
        } else {
            VerticallyScrollableContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .safeContentPadding(),
                scrollState = listState,
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(dotnetSdks, key = { it.pathString }) { localSdk ->
                        DotnetSdkBubble(
                            localSdk,
                            viewModel,
                            Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySdkListPlaceholder(
    placeholderText: String = "Unable to find local .NET SDKs.",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = placeholderText,
            style = JewelTheme.defaultTextStyle.copy(
                color = JewelTheme.globalColors.text.disabled,
                fontSize = 16.sp
            )
        )
    }
}

@Composable
private fun DotnetSdkBubble(
    dotnetSdk: DotnetSdk,
    viewModel: WarehouseViewModelApi,
    modifier: Modifier = Modifier
) {
    val localSdkShape = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 8.dp,
        bottomStart = 8.dp,
        bottomEnd = 8.dp
    )

    val showPopup = remember { mutableStateOf(false) }
    val popupPosition = remember { mutableStateOf(IntOffset.Zero) }
    val itemPosition = remember { mutableStateOf(Offset.Zero) }

    Row(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
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
            },
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .fillMaxWidth()
                .background(
                    JewelTheme.colorPalette.gray(3),
                    localSdkShape
                )
                .padding(16.dp)
        ) {
            DotnetSdkVersion(
                dotnetSdk,
//                onMoreClick = {
//                    // Place popup near the top-right by using a very large X and small Y;
//                    // the position provider will clamp it within the bubble bounds.
//                    clickOffsetState.value = Offset(Float.MAX_VALUE, 0f)
//                    showMenuState.value = true
//                }
            )
            DotnetSdkPath(dotnetSdk)
        }
    }

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
            ContextPopupMenuItem(
                WarehouseBundle.message("local.sdk.bubble.context.menu.delete.option"),
                AllIconsKeys.General.Delete
            ) {
                showPopup.value = false
                viewModel.onDeleteSdk(dotnetSdk)
            }
        }
    }
}

@Composable
private fun DotnetSdkVersion(dotnetSdk: DotnetSdk) {
    Text(
        text = dotnetSdk.version,
        style = JewelTheme.defaultTextStyle.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = JewelTheme.globalColors.text.normal,
            lineHeight = 20.sp
        ),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun DotnetSdkPath(dotnetSdk: DotnetSdk) {
    Text(
        text = dotnetSdk.path.absolutePathString(),
        style = JewelTheme.defaultTextStyle.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = JewelTheme.globalColors.text.info,
            lineHeight = 20.sp
        )
    )
}
