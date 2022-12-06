package com.hoc.flowmvi.mvi_base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hoc.flowmvi.core_ui.collectIn
import com.hoc.flowmvi.core_ui.debugCheckImmediateMainDispatcher
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class AbstractMviActivity<
  I : MviIntent,
  S : MviViewState,
  E : MviSingleEvent,
  VM : MviViewModel<I, S, E>,
  >(
  @LayoutRes contentLayoutId: Int,
) :
  AppCompatActivity(contentLayoutId), MviView<I, S, E> {
  protected abstract val vm: VM

  @CallSuper
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    // observe view model
    vm.viewState
      .collectIn(this) { render(it) }

    // observe single event
    vm.singleEvent
      .collectIn(this) {
        debugCheckImmediateMainDispatcher()
        handleSingleEvent(it)
      }

    // pass view intent to view model
    viewIntents()
      .onEach { vm.processIntent(it) }
      .launchIn(lifecycleScope)
  }

  protected abstract fun setupViews()
}
