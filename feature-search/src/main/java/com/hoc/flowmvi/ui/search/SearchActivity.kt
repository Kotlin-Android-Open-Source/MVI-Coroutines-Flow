package com.hoc.flowmvi.ui.search

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.hoc.flowmvi.core.SearchViewQueryTextEvent
import com.hoc.flowmvi.core.clicks
import com.hoc.flowmvi.core.collectIn
import com.hoc.flowmvi.core.navigator.IntentProviders
import com.hoc.flowmvi.core.queryTextEvents
import com.hoc.flowmvi.core.toast
import com.hoc.flowmvi.ui.search.databinding.ActivitySearchBinding
import com.hoc081098.viewbindingdelegate.viewBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalTime
class SearchActivity : AppCompatActivity(R.layout.activity_search) {
  private val binding by viewBinding<ActivitySearchBinding>()
  private val vm by viewModel<SearchVM>()

  private val searchViewQueryTextEventChannel = Channel<SearchViewQueryTextEvent>()
  private val searchAdapter = SearchAdapter()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    vm.viewState.collectIn(this) { viewState ->
      searchAdapter.submitList(viewState.users)

      binding.run {
        textQuery.isInvisible = viewState.isLoading || viewState.query.isBlank()
        textQuery.text = "Search results for '${viewState.query}'"

        errorGroup.isVisible = viewState.error !== null
        errorMessageTextView.text = viewState.error?.message

        progressBar.isVisible = viewState.isLoading
      }
    }

    vm.singleEvent.collectIn(this) { event ->
      when (event) {
        is SingleEvent.SearchFailure -> toast("Failed to search")
      }
    }

    intents()
      .onEach { vm.processIntent(it) }
      .launchIn(lifecycleScope)
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun intents(): Flow<ViewIntent> = merge(
    searchViewQueryTextEventChannel
      .consumeAsFlow()
      .onEach { Log.d("SearchActivity", "Query $it") }
      .map { ViewIntent.Search(it.query.toString()) },
    binding.retryButton.clicks().map { ViewIntent.Retry },
  )

  private fun setupViews() {
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

    (menu.findItem(R.id.action_search)!!.actionView as SearchView).run {
      isIconified = false
      queryHint = "Search user..."

      queryTextEvents()
        .onEach { searchViewQueryTextEventChannel.send(it) }
        .launchIn(lifecycleScope)
    }

    return true
  }

  internal class IntentProvider : IntentProviders.Search {
    override fun makeIntent(context: Context): Intent =
      Intent(context, SearchActivity::class.java)
  }
}
