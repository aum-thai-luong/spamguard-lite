package com.spamguard.core.data.di

import android.content.Context
import androidx.room.Room
import com.spamguard.core.data.db.BlockRuleDao
import com.spamguard.core.data.db.SpamGuardDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SpamGuardDatabase =
        Room.databaseBuilder(context, SpamGuardDatabase::class.java, SpamGuardDatabase.NAME).build()

    @Provides
    fun provideBlockRuleDao(database: SpamGuardDatabase): BlockRuleDao = database.blockRuleDao()
}
