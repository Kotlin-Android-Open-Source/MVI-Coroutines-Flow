package com.hoc.flowmvi.ui.search

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hoc.flowmvi.core.navigator.IntentProviders
import com.hoc.flowmvi.ui.search.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
  private val addBinding by lazy(LazyThreadSafetyMode.NONE) { ActivitySearchBinding.inflate(layoutInflater) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(addBinding.root)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
  }

  internal class IntentProvider : IntentProviders.Search {
    override fun makeIntent(context: Context): Intent =
      Intent(context, SearchActivity::class.java)
  }
}
