package com.hoc.flowmvi.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hoc.flowmvi.core.collectIn
import com.hoc.flowmvi.core.navigator.IntentProviders
import com.hoc.flowmvi.ui.search.databinding.ActivitySearchBinding
import com.hoc081098.viewbindingdelegate.viewBinding
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalTime
class SearchActivity : AppCompatActivity(R.layout.activity_search) {
  private val binding by viewBinding<ActivitySearchBinding>()
  private val vm by viewModel<SearchVM>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    setupViews()
    bindVM()
  }

  private fun bindVM() {
    vm.viewState.collectIn(this) {
      Log.d("SearchActivity", it.toString())
    }

    vm.singleEvent.collectIn(this) {
      Log.d("SearchActivity", it.toString())
    }

    flowOf(ViewIntent.Search("hoc"))
      .onEach { vm.processIntent(it) }
      .launchIn(lifecycleScope)
  }

  private fun setupViews() {
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> finish().let { true }
      else -> super.onOptionsItemSelected(item)
    }
  }

  internal class IntentProvider : IntentProviders.Search {
    override fun makeIntent(context: Context): Intent =
      Intent(context, SearchActivity::class.java)
  }
}
