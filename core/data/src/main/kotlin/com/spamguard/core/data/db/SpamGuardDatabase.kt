package com.spamguard.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BlockRuleEntity::class],
    version = 1,
    exportSchema = true,
)
internal abstract class SpamGuardDatabase : RoomDatabase() {
    abstract fun blockRuleDao(): BlockRuleDao

    companion object {
        const val NAME = "spamguard.db"
    }
}
