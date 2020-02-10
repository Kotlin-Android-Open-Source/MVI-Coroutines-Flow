package com.hoc.flowmvi.ui.add

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.hoc.flowmvi.databinding.ActivityAddBinding
import com.hoc.flowmvi.textChanges
import com.hoc.flowmvi.ui.add.AddContract.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.koin.androidx.viewmodel.ext.android.viewModel

@FlowPreview
@ExperimentalCoroutinesApi
class AddActivity : AppCompatActivity(), View {
  private val addVM by viewModel<AddVM>()
  private val addBinding by lazy { ActivityAddBinding.inflate(layoutInflater) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(addBinding.root)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)


    setupViews()
    bindVM()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> true.also { finish() }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun bindVM() {
    // observe view model
    addVM.viewState.observe(this, Observer { render(it ?: return@Observer) })
    addVM.singleEvent.observe(
      this,
      Observer { handleSingleEvent(it?.getContentIfNotHandled() ?: return@Observer) }
    )

    // pass view intent to view model
    intents()
      .onEach { addVM.processIntent(it) }
      .launchIn(lifecycleScope)
  }

  private fun handleSingleEvent(event: SingleEvent) {

  }

  private fun render(viewState: ViewState) {
    Log.d("###", viewState.toString())
  }

  private fun setupViews() {

  }


  override fun intents(): Flow<ViewIntent> {
    return merge(
      addBinding
        .emailEditText
        .textChanges()
        .map { ViewIntent.EmailChanged(it?.toString()) },
      addBinding
        .firstNameEditText
        .textChanges()
        .map { ViewIntent.FirstNameChanged(it?.toString()) },
      addBinding
        .lastNameEditText
        .textChanges()
        .map { ViewIntent.LastNameChanged(it?.toString()) }
    )
  }
}