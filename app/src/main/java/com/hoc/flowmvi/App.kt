package com.hoc.flowmvi

import android.app.Application
import com.hoc.flowmvi.core.coreModule
import com.hoc.flowmvi.data.dataModule
import com.hoc.flowmvi.domain.domainModule
import com.hoc.flowmvi.ui.add.addModule
import com.hoc.flowmvi.ui.main.mainModule
import com.hoc.flowmvi.ui.search.searchModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber
import kotlin.time.ExperimentalTime

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
@ExperimentalTime
val allModules = listOf(
  coreModule,
  dataModule,
  domainModule,
  mainModule,
  addModule,
  searchModule,
)

@Suppress("unused")
@ExperimentalStdlibApi
@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalTime
class App : Application() {
  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    } else {
      // TODO(Timber): plant release tree
    }

    startKoin {
      androidContext(this@App)

      androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)

      modules(allModules)
    }
  }
}
