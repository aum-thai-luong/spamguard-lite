package com.spamguard.feature.blocklist

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.spamguard.feature.blocklist.navigation.BlocklistKey

/** Đăng ký màn hình của :feature:blocklist vào `entryProvider` của `NavDisplay` trong :app. */
fun EntryProviderScope<NavKey>.blocklistEntries() {
    entry<BlocklistKey> {
        BlocklistRoute()
    }
}
