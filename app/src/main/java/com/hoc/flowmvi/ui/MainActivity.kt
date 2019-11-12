package com.hoc.flowmvi.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hoc.flowmvi.clicks
import com.hoc.flowmvi.databinding.ActivityMainBinding
import com.hoc.flowmvi.merge
import com.hoc.flowmvi.refreshes
import com.hoc.flowmvi.ui.MainContract.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.LazyThreadSafetyMode.NONE

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), View {
  private val mainVM by viewModel<MainVM>()

  private val userAdapter = UserAdapter()
  private val mainBinding by lazy(NONE) { ActivityMainBinding.inflate(layoutInflater) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(mainBinding.root)

    setupViews()
    bindVM()
  }

  private fun setupViews() {
    mainBinding.usersRecycler.run {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
      adapter = userAdapter
      addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
    }
  }

  private fun bindVM() {
    // observe view model
    mainVM.viewState.observe(this, Observer { render(it ?: return@Observer) })
    mainVM.singleEvent.observe(
      this,
      Observer { handleSingleEvent(it?.getContentIfNotHandled() ?: return@Observer) }
    )

    // pass view intent to view model
    intents()
      .onEach { mainVM.processIntent(it) }
      .launchIn(lifecycleScope)
  }

  override fun intents() = merge(
    flowOf(ViewIntent.Initial),
    mainBinding.swipeRefreshLayout.refreshes().map { ViewIntent.Refresh },
    mainBinding.retryButton.clicks().map { ViewIntent.Retry }
  )

  private fun handleSingleEvent(event: SingleEvent) {
    Log.d("MainActivity", "handleSingleEvent $event")

    when (event) {
      SingleEvent.Refresh.Success -> {
        Toast.makeText(this@MainActivity, "Refresh success", Toast.LENGTH_SHORT).show()
      }
      is SingleEvent.Refresh.Failure -> {
        Toast.makeText(this@MainActivity, "Refresh failure", Toast.LENGTH_SHORT).show()

      }
      is SingleEvent.GetUsersError -> {
        Toast.makeText(this@MainActivity, "Get user failure", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun render(viewState: ViewState) {
    Log.d("MainActivity", "render $viewState")

    userAdapter.submitList(viewState.userItems)

    mainBinding.errorGroup.isVisible = viewState.error !== null
    mainBinding.errorMessageTextView.text = viewState.error?.message

    mainBinding.progressBar.isVisible = viewState.isLoading

    if (viewState.isRefreshing) {
      mainBinding.swipeRefreshLayout.post { mainBinding.swipeRefreshLayout.isRefreshing = true }
    } else {
      mainBinding.swipeRefreshLayout.isRefreshing = false
    }
  }

}
