package com.hoc.flowmvi.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.hoc.flowmvi.R
import com.hoc.flowmvi.ui.MainContract.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.koin.androidx.viewmodel.ext.android.viewModel

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), View {
  private val mainVM by viewModel<MainVM>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    mainVM.viewState.observe(this, Observer {
      Log.d("###", "VS = $it")
    })

    intents()
      .onEach { mainVM.processIntent(it) }
      .launchIn(lifecycleScope)
  }

  override fun intents(): Flow<ViewIntent> {
    val flows = listOf(
      flowOf(ViewIntent.Initial)
    )
    return flows.asFlow().flattenMerge(flows.size)
  }
}
