@file:Suppress("unused")

package com.hoc.flowmvi.initializer

import android.content.Context
import androidx.startup.Initializer
import com.hoc.flowmvi.BuildConfig
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    } else {
      // TODO(Timber): plant release tree
    }
    Timber.d("TimberInitializer...")
  }

  override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
