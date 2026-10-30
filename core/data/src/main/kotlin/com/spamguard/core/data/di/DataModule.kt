package com.spamguard.core.data.di

import com.spamguard.core.data.calllog.CallLogDataSource
import com.spamguard.core.data.calllog.ContentResolverCallLogDataSource
import com.spamguard.core.data.repository.BlockRuleRepositoryImpl
import com.spamguard.core.data.repository.CallLogRepositoryImpl
import com.spamguard.core.domain.repository.BlockRuleRepository
import com.spamguard.core.domain.repository.CallLogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Nối interface của domain với implementation của tầng data. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindCallLogRepository(impl: CallLogRepositoryImpl): CallLogRepository

    @Binds
    internal abstract fun bindBlockRuleRepository(impl: BlockRuleRepositoryImpl): BlockRuleRepository

    @Binds
    internal abstract fun bindCallLogDataSource(impl: ContentResolverCallLogDataSource): CallLogDataSource
}
