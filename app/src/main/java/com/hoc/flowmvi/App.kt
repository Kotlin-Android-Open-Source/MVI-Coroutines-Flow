package com.hoc.flowmvi

import android.app.Application
import com.hoc.flowmvi.core.coreModule
import com.hoc.flowmvi.data.dataModule
import com.hoc.flowmvi.domain.domainModule
import com.hoc.flowmvi.ui.add.addModule
import com.hoc.flowmvi.ui.main.mainModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.ExperimentalTime

@Suppress("unused")
@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalTime
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
