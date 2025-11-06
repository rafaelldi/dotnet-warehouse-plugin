@file:OptIn(ExperimentalJewelApi::class, ExperimentalComposeUiApi::class)

package me.rafaelldi.dotnet.warehouse.toolWindow

import androidx.compose.foundation.background
import androidx.compose.foundation.onClick
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import me.rafaelldi.dotnet.warehouse.local.LocalSdk
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.jetbrains.jewel.ui.theme.colorPalette
import org.jetbrains.jewel.ui.icon.IconKey
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.window.PopupPositionProvider
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import kotlin.io.path.absolutePathString

@Composable
internal fun LocalSdksTab(viewModel: WarehouseViewModel) {
    val localSdks by viewModel.localSdkFlow.collectAsState(emptyList())

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.onReloadLocalSdks()
    }

    Column(
        Modifier
            .fillMaxWidth()
    ) {
        LocalSdkList(
            Modifier
                .fillMaxSize(),
            localSdks,
            viewModel.project,
            listState
        )
    }
}

@Composable
private fun LocalSdkList(
    modifier: Modifier,
    localSdks: List<LocalSdk>,
    project: com.intellij.openapi.project.Project,
    listState: LazyListState,
) {
    Box(modifier = modifier) {
        if (localSdks.isEmpty()) {
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
                    items(localSdks, key = { it.pathString }) { localSdk ->
                        LocalSdkBubble(
                            localSdk,
                            project,
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
private fun LocalSdkBubble(
    localSdk: LocalSdk,
    project: com.intellij.openapi.project.Project,
    modifier: Modifier = Modifier
) {
    val localSdkShape = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 8.dp,
        bottomStart = 8.dp,
        bottomEnd = 8.dp
    )

    // State for context popup menu
    val showMenuState = remember { mutableStateOf(false) }
    val clickOffsetState = remember { mutableStateOf(Offset.Zero) }

    // Position provider that shows popup near the mouse click inside the bubble
    val positionProvider = remember(clickOffsetState.value) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val x = (anchorBounds.left + clickOffsetState.value.x.toInt()).coerceIn(0, (anchorBounds.right - 1).coerceAtLeast(0))
                val y = (anchorBounds.top + clickOffsetState.value.y.toInt()).coerceIn(0, (anchorBounds.bottom - 1).coerceAtLeast(0))
                // Try to keep the popup inside the window by shifting left/up if needed
                val finalX = if (x + popupContentSize.width > anchorBounds.right) anchorBounds.right - popupContentSize.width else x
                val finalY = if (y + popupContentSize.height > anchorBounds.bottom) anchorBounds.bottom - popupContentSize.height else y
                return IntOffset(finalX.coerceAtLeast(anchorBounds.left), finalY.coerceAtLeast(anchorBounds.top))
            }
        }
    }

    Row(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .onPointerEvent(PointerEventType.Press) { event ->
                if (event.buttons.isSecondaryPressed) {
                    // Remember click position relative to the bubble and show menu
                    val pos = event.changes.firstOrNull()?.position ?: Offset.Zero
                    clickOffsetState.value = pos
                    showMenuState.value = true
                }
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
            LocalSdkVersion(
                localSdk,
                onMoreClick = {
                    // Place popup near the top-right by using a very large X and small Y;
                    // the position provider will clamp it within the bubble bounds.
                    clickOffsetState.value = Offset(Float.MAX_VALUE, 0f)
                    showMenuState.value = true
                }
            )
            LocalSdkPath(localSdk)
        }
    }

    if (showMenuState.value) {
        ContextPopupMenu(
            popupPositionProvider = positionProvider,
            onDismissRequest = { showMenuState.value = false }
        ) {
            Column(
                modifier = Modifier
                    .background(JewelTheme.colorPalette.gray(1))
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                ContextPopupMenuItem(
                    actionText = "No-op action",
                    onClick = {
                        // show info notification and close the menu
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("Dotnet Warehouse")
                            .createNotification(
                                title = "Dotnet Warehouse",
                                content = "Action executed",
                                type = NotificationType.INFORMATION
                            )
                            .notify(project)
                        showMenuState.value = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LocalSdkVersion(
    localSdk: LocalSdk,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = localSdk.version,
            style = JewelTheme.defaultTextStyle.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = JewelTheme.globalColors.text.normal,
                lineHeight = 20.sp
            )
        )

        // Three-dots icon button (uses resource icon, not Text)
        Image(
            painter = painterResource("icons/more_vert.svg"),
            contentDescription = "More actions",
            modifier = Modifier
                .size(16.dp)
                .onClick {
                    onMoreClick()
                }
        )
    }
}

@Composable
private fun LocalSdkPath(localSdk: LocalSdk) {
    Text(
        text = localSdk.path.absolutePathString(),
        style = JewelTheme.defaultTextStyle.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = JewelTheme.globalColors.text.info,
            lineHeight = 20.sp
        )
    )
}
