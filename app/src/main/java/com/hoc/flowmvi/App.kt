package com.hoc.flowmvi

import android.app.Application
import com.hoc.flowmvi.koin.dataModule
import com.hoc.flowmvi.koin.domainModule
import com.hoc.flowmvi.koin.viewModelModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

@Suppress("unused")
@FlowPreview
@ExperimentalCoroutinesApi
class App : Application() {
  override fun onCreate() {
    super.onCreate()

    startKoin {
      androidContext(this@App)

      androidLogger()

      modules(
        dataModule,
        domainModule,
        viewModelModule
      )
    }
  }
}