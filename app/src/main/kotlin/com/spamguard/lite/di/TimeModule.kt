package com.spamguard.lite.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.time.ZoneId

/** Cung cấp thời gian qua DI để code nghiệp vụ và test không phụ thuộc đồng hồ hệ thống. */
@Module
@InstallIn(SingletonComponent::class)
object TimeModule {

    @Provides
    fun provideClock(): Clock = Clock.systemUTC()

    /** Không scope: luôn lấy múi giờ hiện tại của thiết bị. */
    @Provides
    fun provideZoneId(): ZoneId = ZoneId.systemDefault()
}
