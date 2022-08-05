package com.hoc.flowmvi

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.hoc.flowmvi.core.coreModule
import com.hoc.flowmvi.data.dataModule
import com.hoc.flowmvi.domain.domainModule
import com.hoc.flowmvi.ui.add.addModule
import com.hoc.flowmvi.ui.main.mainModule
import com.hoc.flowmvi.ui.search.searchModule
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
@ExperimentalTime
@JvmField
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

    DynamicColors.applyToActivitiesIfAvailable(this)

    startKoin {
      androidContext(this@App)

      androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)

      modules(allModules)
    }
  }
}
