package com.spamguard.core.data.di

import javax.inject.Qualifier

/** Dispatcher cho I/O (content provider, file). Inject để test thay được. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher
