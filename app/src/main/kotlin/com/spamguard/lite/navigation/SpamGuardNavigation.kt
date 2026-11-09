package com.spamguard.lite.navigation

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.spamguard.feature.blocklist.blocklistEntries
import com.spamguard.feature.blocklist.navigation.BlocklistKey
import com.spamguard.feature.calllog.callLogEntries
import com.spamguard.feature.calllog.navigation.CallDetailKey
import com.spamguard.feature.calllog.navigation.CallLogKey
import com.spamguard.lite.R

/** Tab trên thanh điều hướng dưới; mỗi tab là gốc của back stack. */
internal enum class TopLevelDestination(
    val key: NavKey,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    /** :baselineprofile tìm tab Danh sách chặn bằng tag này; giữ ổn định. */
    val testTag: String,
) {
    CallLog(CallLogKey, R.drawable.ic_nav_call_log, R.string.destination_call_log, "callLogFragment"),
    Blocklist(BlocklistKey, R.drawable.ic_nav_blocklist, R.string.destination_blocklist, "blocklistFragment"),
}

/**
 * Khung app: thanh trên, thanh điều hướng dưới và `NavDisplay` của Navigation 3.
 * Back stack là một danh sách [NavKey] được lưu lại khi xoay màn hình hoặc hệ thống dừng process;
 * mỗi màn hình có ViewModel riêng, bị xóa khi màn hình rời back stack.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SpamGuardNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(CallLogKey)
    val currentTab = backStack.first()
    val navigateBack: () -> Unit = {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    // Ở gốc tab khác tab Cuộc gọi, Back quay về tab Cuộc gọi thay vì thoát app.
    BackHandler(enabled = backStack.size == 1 && currentTab != CallLogKey) {
        backStack.navigateToTab(CallLogKey)
    }

    Scaffold(
        // Lộ testTag thành resource-id để UiAutomator (Baseline Profile, macrobenchmark) tìm được.
        modifier = modifier.semantics { testTagsAsResourceId = true },
        topBar = {
            SpamGuardTopBar(
                currentKey = backStack.last(),
                canNavigateBack = backStack.size > 1,
                onNavigateBack = navigateBack,
            )
        },
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentTab == destination.key,
                        onClick = { backStack.navigateToTab(destination.key) },
                        icon = { Icon(painterResource(destination.iconRes), contentDescription = null) },
                        label = { Text(stringResource(destination.labelRes)) },
                        modifier = Modifier.testTag(destination.testTag),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = navigateBack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                callLogEntries(onNavigate = backStack::add, onBack = navigateBack)
                blocklistEntries()
            },
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpamGuardTopBar(
    currentKey: NavKey,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
) {
    val titleRes = when (currentKey) {
        CallLogKey -> R.string.destination_call_log
        BlocklistKey -> R.string.destination_blocklist
        is CallDetailKey -> R.string.destination_call_detail
        else -> R.string.app_name
    }
    TopAppBar(
        title = { Text(stringResource(titleRes)) },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.navigate_up),
                    )
                }
            }
        },
    )
}

/** Chuyển tab: back stack chỉ còn gốc của tab mới, giống `popUpTo` của thanh điều hướng dưới. */
private fun NavBackStack<NavKey>.navigateToTab(tab: NavKey) {
    if (size == 1 && first() == tab) return
    clear()
    add(tab)
}
