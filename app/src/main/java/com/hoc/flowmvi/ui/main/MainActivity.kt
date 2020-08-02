package com.hoc.flowmvi.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hoc.flowmvi.*
import com.hoc.flowmvi.databinding.ActivityMainBinding
import com.hoc.flowmvi.ui.add.AddActivity
import com.hoc.flowmvi.ui.main.MainContract.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlin.LazyThreadSafetyMode.NONE

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View {
  private val mainVM by viewModels<MainVM>()

  private val userAdapter = UserAdapter()
  private val mainBinding by lazy(NONE) { ActivityMainBinding.inflate(layoutInflater) }

  private val removeChannel = BroadcastChannel<UserItem>(Channel.BUFFERED)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(mainBinding.root)

    setupViews()
    bindVM(mainVM)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.add_action -> {
        startActivity(Intent(this, AddActivity::class.java))
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
            removeChannel.offer(userItem)
          }
      ).attachToRecyclerView(this)
    }
  }

  private fun bindVM(mainVM: MainVM) {
    // observe view model
    lifecycleScope.launchWhenStarted {
      mainVM.viewState
          .onEach { render(it) }
          .catch { }
          .collect()
    }
    lifecycleScope.launchWhenStarted {
      mainVM.singleEvent
          .onEach { handleSingleEvent(it) }
          .catch { }
          .collect()
    }

    // pass view intent to view model
    intents()
        .onEach { mainVM.processIntent(it) }
        .launchIn(lifecycleScope)
  }

  override fun intents() = merge(
      flowOf(ViewIntent.Initial),
      mainBinding.swipeRefreshLayout.refreshes().map { ViewIntent.Refresh },
      mainBinding.retryButton.clicks().map { ViewIntent.Retry },
      removeChannel.asFlow().map { ViewIntent.RemoveUser(it) }
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
