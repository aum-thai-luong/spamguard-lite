package com.spamguard.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "block_rules",
    indices = [Index(value = ["pattern", "match_type"], unique = true)],
)
internal data class BlockRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pattern: String,
    /** Tên của [com.spamguard.core.domain.model.MatchType]. */
    @ColumnInfo(name = "match_type")
    val matchType: String,
    @ColumnInfo(name = "created_at")
    val createdAtEpochMillis: Long,
)
