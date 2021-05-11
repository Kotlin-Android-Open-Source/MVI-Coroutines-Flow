package com.hoc.flowmvi.ui.search

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hoc.flowmvi.core.navigator.IntentProviders

class SearchActivity : AppCompatActivity(R.layout.activity_search) {

  internal class IntentProvider : IntentProviders.Search {
    override fun makeIntent(context: Context): Intent =
      Intent(context, SearchActivity::class.java)
  }
}
