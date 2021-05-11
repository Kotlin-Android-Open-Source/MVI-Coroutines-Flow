package com.hoc.flowmvi.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.hoc.flowmvi.core.navigator.IntentProviders
import com.hoc.flowmvi.ui.search.databinding.ActivitySearchBinding
import com.hoc081098.viewbindingdelegate.viewBinding

class SearchActivity : AppCompatActivity(R.layout.activity_search) {
  private val binding by viewBinding<ActivitySearchBinding>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
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
