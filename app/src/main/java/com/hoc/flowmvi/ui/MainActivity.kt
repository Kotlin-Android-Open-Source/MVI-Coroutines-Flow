package com.hoc.flowmvi.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hoc.flowmvi.*
import com.hoc.flowmvi.databinding.ActivityMainBinding
import com.hoc.flowmvi.ui.MainContract.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.LazyThreadSafetyMode.NONE

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), View {
  private val mainVM by viewModel<MainVM>()

  private val userAdapter = UserAdapter()
  private val mainBinding by lazy(NONE) { ActivityMainBinding.inflate(layoutInflater) }

  private val removeChannel = Channel<UserItem>(Channel.UNLIMITED)

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

      ItemTouchHelper(
        SwipeLeftToDeleteCallback(context) cb@{ position ->
          val userItem = mainVM.viewState.value?.userItems?.get(position) ?: return@cb
          removeChannel.offer(userItem)
        }
      ).attachToRecyclerView(this)
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
    mainBinding.retryButton.clicks().map { ViewIntent.Retry },
    removeChannel.consumeAsFlow().map { ViewIntent.RemoveUser(it) }
  )

  private fun handleSingleEvent(event: SingleEvent) {
    Log.d("MainActivity", "handleSingleEvent $event")
    return when (event) {
      SingleEvent.Refresh.Success -> toast("Refresh success")
      is SingleEvent.Refresh.Failure -> toast("Refresh failure")
      is SingleEvent.GetUsersError -> toast("Get user failure")
      is SingleEvent.RemoveUser.Success -> toast("Removed '${event.user.fullName}'")
      is SingleEvent.RemoveUser.Failure -> toast("Error when removing '${event.user.fullName}'")
    }
  }

  private fun render(viewState: ViewState) {
    Log.d("MainActivity", "render $viewState")

    userAdapter.submitList(viewState.userItems)

    mainBinding.run {
      errorGroup.isVisible = viewState.error !== null
      errorMessageTextView.text = viewState.error?.message

      progressBar.isVisible = viewState.isLoading

      if (viewState.isRefreshing) {
        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = true }
      } else {
        swipeRefreshLayout.isRefreshing = false
      }

      swipeRefreshLayout.isEnabled = !viewState.isLoading && viewState.error === null
    }
  }
}
