@file:Suppress("unused")

package com.hoc.flowmvi.initializer

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.hoc.flowmvi.BuildConfig
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    } else {
      Timber.plant(ReleaseTree())
    }
    Timber.d("TimberInitializer...")
  }

  override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

/**
 * A Timber tree for release builds that only logs warnings and errors.
 * This prevents sensitive information from being logged in production.
 */
private class ReleaseTree : Timber.Tree() {
  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
      return
    }

    // Log warnings and errors to system log
    // In a production app, you might want to send these to a crash reporting service
    // like Firebase Crashlytics, Sentry, etc.
    if (priority == Log.ERROR && t != null) {
      // You could send to crash reporting service here
      Log.e(tag, message, t)
    } else if (priority == Log.WARN) {
      Log.w(tag, message)
    } else if (priority == Log.ERROR) {
      Log.e(tag, message)
    }
  }
}
