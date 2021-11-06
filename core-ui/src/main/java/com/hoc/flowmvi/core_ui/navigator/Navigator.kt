package com.hoc.flowmvi.core_ui.navigator

import android.content.Context
import android.content.Intent

interface IntentProviders {
  interface Add {
    fun makeIntent(context: Context): Intent
  }

  interface Search {
    fun makeIntent(context: Context): Intent
  }
}

interface Navigator {
  fun Context.navigateToAdd()

  fun Context.navigateToSearch()
}
