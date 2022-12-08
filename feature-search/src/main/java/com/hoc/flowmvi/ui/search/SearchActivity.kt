package com.hoc.flowmvi.ui.search

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.hoc.flowmvi.core_ui.SearchViewQueryTextEvent
import com.hoc.flowmvi.core_ui.clicks
import com.hoc.flowmvi.core_ui.navigator.IntentProviders
import com.hoc.flowmvi.core_ui.queryTextEvents
import com.hoc.flowmvi.core_ui.toast
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.mvi_base.AbstractMviActivity
import com.hoc.flowmvi.ui.search.databinding.ActivitySearchBinding
import com.hoc081098.viewbindingdelegate.viewBinding
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import timber.log.Timber

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalTime
class SearchActivity :
  AbstractMviActivity<ViewIntent, ViewState, SingleEvent, SearchVM>(R.layout.activity_search) {
  private val binding by viewBinding<ActivitySearchBinding>()
  override val vm by stateViewModel<SearchVM>()

  private val searchViewQueryTextEventChannel = Channel<SearchViewQueryTextEvent>()
  private val searchAdapter = SearchAdapter()

  override fun handleSingleEvent(event: SingleEvent) {
    when (event) {
      is SingleEvent.SearchFailure -> toast("Failed to search")
    }
  }

  override fun render(viewState: ViewState) {
    searchAdapter.submitList(viewState.users)

    binding.run {
      textQuery.isInvisible = viewState.isLoading || viewState.submittedQuery.isBlank()
      if (textQuery.isVisible) {
        textQuery.text = "Search results for '${viewState.submittedQuery}'"
      }

      TransitionManager.endTransitions(root)
      TransitionManager.beginDelayedTransition(
        root,
        AutoTransition()
          .addTarget(errorGroup)
          .addTarget(progressBar)
          .setDuration(200)
      )

      errorGroup.isVisible = viewState.error !== null
      if (errorGroup.isVisible) {
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
      }

      progressBar.isVisible = viewState.isLoading
    }
  }

  override fun viewIntents(): Flow<ViewIntent> = merge(
    searchViewQueryTextEventChannel
      .consumeAsFlow()
      .onEach { Timber.d(">>> Query $it") }
      .map { ViewIntent.Search(it.query.toString()) },
    binding.retryButton.clicks().map { ViewIntent.Retry },
  )

  override fun setupViews() {
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.run {
      usersRecycler.run {
        setHasFixedSize(true)
        layoutManager = GridLayoutManager(
          context,
          if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 4,
        )
        adapter = searchAdapter
      }
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> finish().let { true }
      else -> super.onOptionsItemSelected(item)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_search, menu)

    menu.findItem(R.id.action_search)!!.let { menuItem ->
      (menuItem.actionView as SearchView).run {
        isIconified = false
        queryHint = "Search user..."

        Timber.d("onCreateOptionsMenu: originalQuery=${ vm.viewState.value.originalQuery}")
        vm.viewState.value
          .originalQuery
          .takeIf { it.isNotBlank() }
          ?.let {
            menuItem.expandActionView()
            setQuery(it, true)
            clearFocus()
          }

        queryTextEvents()
          .onEach { searchViewQueryTextEventChannel.send(it) }
          .launchIn(lifecycleScope)
      }
    }

    return true
  }

  internal class IntentProvider : IntentProviders.Search {
    override fun makeIntent(context: Context): Intent =
      Intent(context, SearchActivity::class.java)
  }
}
