package com.hoc.flowmvi.ui.main

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hoc.flowmvi.core.SwipeLeftToDeleteCallback
import com.hoc.flowmvi.core.clicks
import com.hoc.flowmvi.core.launchWhenStartedUntilStopped
import com.hoc.flowmvi.core.navigator.Navigator
import com.hoc.flowmvi.core.refreshes
import com.hoc.flowmvi.core.safeOffer
import com.hoc.flowmvi.core.toast
import com.hoc.flowmvi.ui.main.databinding.ActivityMainBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.LazyThreadSafetyMode.NONE

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {
  private val mainVM by viewModel<MainVM>()
  private val navigator by inject<Navigator>()

  private val userAdapter = UserAdapter()
  private val mainBinding by lazy(NONE) { ActivityMainBinding.inflate(layoutInflater) }

  private val removeChannel = Channel<UserItem>(Channel.BUFFERED)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(mainBinding.root)

    setupViews()
    bindVM(mainVM)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.add_action -> {
        navigator.run { navigateToAdd() }
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  private fun setupViews() {
    mainBinding.usersRecycler.run {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
      adapter = userAdapter
      addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))

      ItemTouchHelper(
        SwipeLeftToDeleteCallback(context) cb@{ position ->
          val userItem = mainVM.viewState.value.userItems[position]
          removeChannel.safeOffer(userItem)
        }
      ).attachToRecyclerView(this)
    }
  }

  private fun bindVM(mainVM: MainVM) {
    // observe view model
    mainVM.viewState
      .onEach { render(it) }
      .launchWhenStartedUntilStopped(this)

    // observe single event
    mainVM.singleEvent
      .onEach { handleSingleEvent(it) }
      .launchWhenStartedUntilStopped(this)

    // pass view intent to view model
    intents()
      .onEach { mainVM.processIntent(it) }
      .launchIn(lifecycleScope)
  }

  private fun intents() = merge(
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
        if (!swipeRefreshLayout.isRefreshing) {
          swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = true }
        }
      } else {
        swipeRefreshLayout.isRefreshing = false
      }

      swipeRefreshLayout.isEnabled = !viewState.isLoading && viewState.error === null
    }
  }
}
