package com.hoc.flowmvi

import android.app.Application
import com.hoc.flowmvi.core.coreModule
import com.hoc.flowmvi.data.dataModule
import com.hoc.flowmvi.domain.domainModule
import com.hoc.flowmvi.ui.add.addModule
import com.hoc.flowmvi.ui.main.mainModule
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

      androidLogger(level = Level.DEBUG)

      modules(
        coreModule,
        dataModule,
        domainModule,
        mainModule,
        addModule,
      )
    }
  }
}
