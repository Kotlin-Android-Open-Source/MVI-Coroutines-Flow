@file:Suppress("unused")

package com.hoc.flowmvi.initializer

import android.content.Context
import androidx.startup.Initializer
import com.hoc.flowmvi.ui.add.databinding.ActivityAddBinding
import com.hoc.flowmvi.ui.main.databinding.ActivityMainBinding
import com.hoc081098.viewbindingdelegate.preloadBindMethods
import timber.log.Timber

class ViewBindingInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    preloadBindMethods(
      ActivityMainBinding::class,
      ActivityAddBinding::class,
    )
    Timber.d("ViewBindingInitializer...")
  }

  override fun dependencies(): List<Class<out Initializer<*>>> =
    listOf(TimberInitializer::class.java)
}
