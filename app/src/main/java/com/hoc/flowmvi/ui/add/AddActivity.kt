package com.hoc.flowmvi.ui.add

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.hoc.flowmvi.clicks
import com.hoc.flowmvi.databinding.ActivityAddBinding
import com.hoc.flowmvi.textChanges
import com.hoc.flowmvi.toast
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
    Log.d("###", "Event=$event")

    return when (event) {
      is SingleEvent.AddUserSuccess -> toast("Add success")
      is SingleEvent.AddUserFailure -> toast("Add failure")
    }
  }

  private fun render(viewState: ViewState) {
    Log.d("###", "ViewState=$viewState")

    val emailErrorMessage = if (ValidationError.INVALID_EMAIL_ADDRESS in viewState.errors) {
      "Invalid email"
    } else {
      null
    }
    if (addBinding.emailEditText.error != emailErrorMessage) {
      addBinding.emailEditText.error = emailErrorMessage
    }

    val firstNameErrorMessage = if (ValidationError.TOO_SHORT_FIRST_NAME in viewState.errors) {
      "Too short first name"
    } else {
      null
    }
    if (addBinding.firstNameEditText.error != firstNameErrorMessage) {
      addBinding.firstNameEditText.error = firstNameErrorMessage
    }

    val lastNameErrorMessage = if (ValidationError.TOO_SHORT_LAST_NAME in viewState.errors) {
      "Too short last name"
    } else {
      null
    }
    if (addBinding.lastNameEditText.error != lastNameErrorMessage) {
      addBinding.lastNameEditText.error = lastNameErrorMessage
    }

    addBinding.progressBar.isInvisible = !viewState.isLoading
  }

  private fun setupViews() {

  }


  override fun intents(): Flow<ViewIntent> {
    return merge(
      addBinding
        .emailEditText
        .editText!!
        .textChanges()
        .map { ViewIntent.EmailChanged(it?.toString()) },
      addBinding
        .firstNameEditText
        .editText!!
        .textChanges()
        .map { ViewIntent.FirstNameChanged(it?.toString()) },
      addBinding
        .lastNameEditText
        .editText!!
        .textChanges()
        .map { ViewIntent.LastNameChanged(it?.toString()) },
      addBinding
        .addButton
        .clicks()
        .map { ViewIntent.Submit }
    )
  }
}