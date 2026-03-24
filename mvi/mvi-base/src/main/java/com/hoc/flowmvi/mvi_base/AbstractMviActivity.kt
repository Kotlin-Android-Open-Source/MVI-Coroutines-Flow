package com.hoc.flowmvi.mvi_base

import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
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
) : AppCompatActivity(contentLayoutId),
  MviView<I, S, E> {
  protected abstract val vm: VM

  @CallSuper
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    //
    enableEdgeToEdge()
    //
    setupViews()
    bindVM()
    //
    adaptFullScreen()
  }

  private fun bindVM() {
    // observe view model
    vm
      .viewState
      .collectIn(this) { render(it) }

    // observe single event
    vm
      .singleEvent
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


  private fun adaptFullScreen() {
    //
    window.decorView.viewTreeObserver.addOnGlobalLayoutListener(
      object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
          //
          window.decorView.viewTreeObserver.removeOnGlobalLayoutListener(this)
          //
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //
            val systemBarsInsets = WindowInsetsCompat.toWindowInsetsCompat(window.decorView.rootWindowInsets).getInsets(WindowInsetsCompat.Type.systemBars())
            //
            val actionBarHeight = supportActionBar?.height ?: 0
            //
            rootView().run {
              val newLayoutParams = layoutParams as? ViewGroup.MarginLayoutParams ?: throw IllegalStateException("root view must have MarginLayoutParams")
              newLayoutParams.topMargin = systemBarsInsets.top + actionBarHeight
              newLayoutParams.bottomMargin = systemBarsInsets.bottom
              layoutParams = newLayoutParams
            }
          }
        }
      },
    )
  }

  protected abstract fun rootView(): ViewGroup
}
