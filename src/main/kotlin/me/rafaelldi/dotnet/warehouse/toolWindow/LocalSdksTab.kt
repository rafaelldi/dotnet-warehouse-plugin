@file:OptIn(ExperimentalJewelApi::class, ExperimentalComposeUiApi::class)

package me.rafaelldi.dotnet.warehouse.toolWindow

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.rafaelldi.dotnet.warehouse.local.LocalSdk
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.jetbrains.jewel.ui.theme.defaultBannerStyle
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
            listState
        )
    }
}

@Composable
private fun LocalSdkList(
    modifier: Modifier,
    localSdks: List<LocalSdk>,
    listState: LazyListState,
) {
    Box(modifier = modifier) {
        if (localSdks.isEmpty()) {
            EmptySdkListPlaceholder()
        } else {
            VerticallyScrollableContainer(
                modifier = Modifier.fillMaxWidth().safeContentPadding(),
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
    modifier: Modifier = Modifier
) {
    val localSdkShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )

    Row(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .fillMaxWidth()
                .background(
                    JewelTheme.defaultBannerStyle.information.colors.background.copy(alpha = 0.75f),
                    localSdkShape
                )
                .padding(16.dp)
        ) {
            LocalSdkVersion(localSdk)
            LocalSdkPath(localSdk)
        }
    }
}

@Composable
private fun LocalSdkVersion(localSdk: LocalSdk) {
    Text(
        text = localSdk.version,
        style = JewelTheme.defaultTextStyle.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = JewelTheme.globalColors.text.normal,
            lineHeight = 20.sp
        ),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun LocalSdkPath(localSdk: LocalSdk) {
    Text(
        text = localSdk.path.absolutePathString(),
        style = JewelTheme.defaultTextStyle.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = JewelTheme.globalColors.text.normal,
            lineHeight = 20.sp
        ),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
