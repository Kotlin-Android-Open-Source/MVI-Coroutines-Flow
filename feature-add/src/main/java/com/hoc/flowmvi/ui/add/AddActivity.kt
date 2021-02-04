package com.hoc.flowmvi.ui.add

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.hoc.flowmvi.core.clicks
import com.hoc.flowmvi.core.firstChange
import com.hoc.flowmvi.core.launchWhenStartedUntilStopped
import com.hoc.flowmvi.core.navigator.IntentProviders
import com.hoc.flowmvi.core.textChanges
import com.hoc.flowmvi.core.toast
import com.hoc.flowmvi.ui.add.databinding.ActivityAddBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.androidx.viewmodel.scope.emptyState
import kotlin.LazyThreadSafetyMode.NONE

@FlowPreview
@ExperimentalCoroutinesApi
class AddActivity : AppCompatActivity() {
  private val addVM by viewModel<AddVM>(state = emptyState())
  private val addBinding by lazy(NONE) { ActivityAddBinding.inflate(layoutInflater) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(addBinding.root)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    setupViews()
    bindVM(addVM)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> true.also { finish() }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun bindVM(addVM: AddVM) {
    // observe view model
    addVM.viewState
      .onEach { render(it) }
      .launchWhenStartedUntilStopped(this)

    // observe single event
    addVM.singleEvent
      .onEach { handleSingleEvent(it) }
      .launchWhenStartedUntilStopped(this)

    // pass view intent to view model
    intents()
      .onEach { addVM.processIntent(it) }
      .launchIn(lifecycleScope)
  }

  private fun handleSingleEvent(event: SingleEvent) {
    Log.d("###", "Event=$event")

    return when (event) {
      is SingleEvent.AddUserSuccess -> {
        toast("Add success")
        finish()
      }
      is SingleEvent.AddUserFailure -> {
        Log.d("###", event.toString())
        toast("Add failure")
      }
    }
  }

  private fun render(viewState: ViewState) {
    Log.d("###", "ViewState=$viewState")

    val emailErrorMessage = if (ValidationError.INVALID_EMAIL_ADDRESS in viewState.errors) {
      "Invalid email"
    } else {
      null
    }
    if (viewState.emailChanged && addBinding.emailEditText.error != emailErrorMessage) {
      addBinding.emailEditText.error = emailErrorMessage
    }

    val firstNameErrorMessage = if (ValidationError.TOO_SHORT_FIRST_NAME in viewState.errors) {
      "Too short first name"
    } else {
      null
    }
    if (viewState.firstNameChanged && addBinding.firstNameEditText.error != firstNameErrorMessage) {
      addBinding.firstNameEditText.error = firstNameErrorMessage
    }

    val lastNameErrorMessage = if (ValidationError.TOO_SHORT_LAST_NAME in viewState.errors) {
      "Too short last name"
    } else {
      null
    }
    if (viewState.lastNameChanged && addBinding.lastNameEditText.error != lastNameErrorMessage) {
      addBinding.lastNameEditText.error = lastNameErrorMessage
    }

    TransitionManager.beginDelayedTransition(
      addBinding.root,
      AutoTransition()
        .addTarget(addBinding.progressBar)
        .addTarget(addBinding.addButton)
        .setDuration(200)
    )
    addBinding.progressBar.isInvisible = !viewState.isLoading
    addBinding.addButton.isInvisible = viewState.isLoading
  }

  private fun setupViews() {
    val state = addVM.viewState.value

    addBinding.run {
      emailEditText.editText!!.setText(state.email)
      firstNameEditText.editText!!.setText(state.firstName)
      lastNameEditText.editText!!.setText(state.lastName)
    }
  }

  private fun intents(): Flow<ViewIntent> = addBinding.run {
    merge(
      emailEditText
        .editText!!
        .textChanges()
        .map { ViewIntent.EmailChanged(it?.toString()) },
      firstNameEditText
        .editText!!
        .textChanges()
        .map { ViewIntent.FirstNameChanged(it?.toString()) },
      lastNameEditText
        .editText!!
        .textChanges()
        .map { ViewIntent.LastNameChanged(it?.toString()) },
      addButton
        .clicks()
        .map { ViewIntent.Submit },
      emailEditText
        .editText!!
        .firstChange()
        .map { ViewIntent.EmailChangedFirstTime },
      firstNameEditText
        .editText!!
        .firstChange()
        .map { ViewIntent.FirstNameChangedFirstTime },
      lastNameEditText
        .editText!!
        .firstChange()
        .map { ViewIntent.LastNameChangedFirstTime },
    )
  }

  @ExperimentalCoroutinesApi
  @FlowPreview
  internal class IntentProvider : IntentProviders.Add {
    override fun makeIntent(context: Context): Intent =
      Intent(context, AddActivity::class.java)
  }
}
