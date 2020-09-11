package com.hoc.flowmvi

import android.app.Application
import com.hoc.flowmvi.koin.allModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

@Suppress("unused")
@FlowPreview
@ExperimentalCoroutinesApi
class App : Application() {
  override fun onCreate() {
    super.onCreate()

    startKoin {
      androidContext(this@App)

      // https://github.com/InsertKoinIO/koin/issues/847
      androidLogger(level = Level.ERROR)

      modules(allModules)
    }
  }
}
