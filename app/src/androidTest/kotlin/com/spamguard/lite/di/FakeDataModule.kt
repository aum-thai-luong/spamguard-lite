package com.spamguard.lite.di

import com.spamguard.core.data.di.DataModule
import com.spamguard.core.domain.model.CallType
import com.spamguard.core.domain.repository.BlockRuleRepository
import com.spamguard.core.domain.repository.CallLogRepository
import com.spamguard.core.testing.FakeBlockRuleRepository
import com.spamguard.core.testing.FakeCallLogRepository
import com.spamguard.core.testing.aCallRecord
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Thay [DataModule] bằng fake trong mọi instrumented test: không đọc nhật ký cuộc gọi thật của
 * thiết bị, không ghi Room. Test inject [FakeCallLogRepository]/[FakeBlockRuleRepository] để đổi dữ liệu.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DataModule::class])
object FakeDataModule {

    @Provides
    @Singleton
    fun provideFakeCallLogRepository(): FakeCallLogRepository = FakeCallLogRepository().apply {
        // Dữ liệu giả; Activity mở ngay khi test bắt đầu nên phải có sẵn từ đây.
        calls = listOf(
            aCallRecord(id = 1, number = "0919009001", contactName = "Chị Lan"),
            aCallRecord(id = 2, number = "0900123456", type = CallType.MISSED),
        )
    }

    @Provides
    @Singleton
    fun provideFakeBlockRuleRepository(): FakeBlockRuleRepository = FakeBlockRuleRepository()

    @Provides
    fun provideCallLogRepository(fake: FakeCallLogRepository): CallLogRepository = fake

    @Provides
    fun provideBlockRuleRepository(fake: FakeBlockRuleRepository): BlockRuleRepository = fake
}
