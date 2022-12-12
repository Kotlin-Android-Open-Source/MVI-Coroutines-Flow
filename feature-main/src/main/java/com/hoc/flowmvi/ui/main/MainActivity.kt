package com.hoc.flowmvi.ui.main

import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.hoc.flowmvi.core_ui.SwipeLeftToDeleteCallback
import com.hoc.flowmvi.core_ui.clicks
import com.hoc.flowmvi.core_ui.dpToPx
import com.hoc.flowmvi.core_ui.navigator.Navigator
import com.hoc.flowmvi.core_ui.refreshes
import com.hoc.flowmvi.core_ui.toast
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.mvi_base.AbstractMviActivity
import com.hoc.flowmvi.ui.main.databinding.ActivityMainBinding
import com.hoc081098.viewbindingdelegate.viewBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity :
  AbstractMviActivity<ViewIntent, ViewState, SingleEvent, MainVM>(R.layout.activity_main) {
  override val vm by viewModel<MainVM>()
  private val navigator by inject<Navigator>()

  private val userAdapter = UserAdapter()
  private val mainBinding by viewBinding<ActivityMainBinding>()

  private val removeChannel = Channel<UserItem>(Channel.BUFFERED)

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.add_action -> {
        navigator.run { navigateToAdd() }
        true
      }
      R.id.search_action -> {
        navigator.run { navigateToSearch() }
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu) =
    menuInflater.inflate(R.menu.menu_main, menu).let { true }

  override fun setupViews() {
    mainBinding.usersRecycler.run {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
      adapter = userAdapter
      addItemDecoration(
        MaterialDividerItemDecoration(context, RecyclerView.VERTICAL).apply {
          dividerInsetStart = dpToPx(8f)
          dividerInsetEnd = dpToPx(8f)
          isLastItemDecorated = false
          dividerThickness = dpToPx(0.8f)
        }
      )

      ItemTouchHelper(
        SwipeLeftToDeleteCallback(context) cb@{ position ->
          val userItem = vm.viewState.value.userItems[position]
          removeChannel.trySend(userItem)
        }
      ).attachToRecyclerView(this)
    }
  }

  override fun viewIntents(): Flow<ViewIntent> = merge(
    flowOf(ViewIntent.Initial),
    mainBinding.swipeRefreshLayout.refreshes().map { ViewIntent.Refresh },
    mainBinding.retryButton.clicks().map { ViewIntent.Retry },
    removeChannel.consumeAsFlow().map { ViewIntent.RemoveUser(it) }
  )

  override fun handleSingleEvent(event: SingleEvent) {
    Timber.d("handleSingleEvent $event")
    return when (event) {
      SingleEvent.Refresh.Success -> toast("Refresh success")
      is SingleEvent.Refresh.Failure -> toast("Refresh failure")
      is SingleEvent.GetUsersError -> toast("Get user failure")
      is SingleEvent.RemoveUser.Success -> toast("Removed '${event.user.fullName}'")
      is SingleEvent.RemoveUser.Failure -> {
        toast("Error when removing '${event.user.fullName}'")
        userAdapter.notifyItemChanged(event.indexProducer() ?: return)
      }
    }
  }

  override fun render(viewState: ViewState) {
    Timber.d("render $viewState")

    userAdapter.submitList(viewState.userItems)

    mainBinding.run {
      errorGroup.isVisible = viewState.error !== null
      errorMessageTextView.text = viewState.error?.let {
        when (it) {
          is UserError.InvalidId -> "Invalid id"
          UserError.NetworkError -> "Network error"
          UserError.ServerError -> "Server error"
          UserError.Unexpected -> "Unexpected error"
          is UserError.UserNotFound -> "User not found"
          is UserError.ValidationFailed -> "Validation failed"
        }
      }

      progressBar.isVisible = viewState.isLoading

      if (viewState.isRefreshing) {
        if (!swipeRefreshLayout.isRefreshing) {
          swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = true }
        }
      } else {
        swipeRefreshLayout.isRefreshing = false
      }

      swipeRefreshLayout.isEnabled = viewState.canRefresh
    }
  }
}
