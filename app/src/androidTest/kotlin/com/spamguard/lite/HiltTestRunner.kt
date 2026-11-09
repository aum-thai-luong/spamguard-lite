package com.spamguard.lite

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/** Chạy instrumented test với [HiltTestApplication] để test thay được module Hilt bằng fake. */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
