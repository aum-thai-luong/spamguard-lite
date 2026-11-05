package com.spamguard.core.data.repository

import com.spamguard.core.data.calllog.CallLogDataSource
import com.spamguard.core.data.calllog.CallLogEntry
import com.spamguard.core.data.di.IoDispatcher
import com.spamguard.core.data.mapper.toDomain
import com.spamguard.core.domain.error.CallLogUnavailableException
import com.spamguard.core.domain.model.CallRecord
import com.spamguard.core.domain.repository.CallLogRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class CallLogRepositoryImpl @Inject constructor(
    private val dataSource: CallLogDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CallLogRepository {

    override suspend fun getRecentCalls(limit: Int): List<CallRecord> = withContext(ioDispatcher) {
        val entries = dataSource.queryRecentCalls(limit)
            ?: throw CallLogUnavailableException("Call log provider returned no cursor")
        entries.map(CallLogEntry::toDomain)
    }

    override suspend fun getCall(id: Long): CallRecord? = withContext(ioDispatcher) {
        val entries = dataSource.queryCall(id)
            ?: throw CallLogUnavailableException("Call log provider returned no cursor")
        entries.firstOrNull()?.toDomain()
    }
}
