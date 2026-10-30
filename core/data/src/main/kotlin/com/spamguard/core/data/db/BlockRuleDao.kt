package com.spamguard.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface BlockRuleDao {

    @Query("SELECT * FROM block_rules ORDER BY created_at DESC")
    fun observeAll(): Flow<List<BlockRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: BlockRuleEntity): Long

    @Query("DELETE FROM block_rules WHERE id = :ruleId")
    suspend fun deleteById(ruleId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM block_rules WHERE pattern = :pattern AND match_type = :matchType)")
    suspend fun exists(pattern: String, matchType: String): Boolean
}
