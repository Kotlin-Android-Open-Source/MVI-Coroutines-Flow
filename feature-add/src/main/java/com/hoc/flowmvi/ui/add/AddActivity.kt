package com.hoc.flowmvi.ui.add

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.core.view.isInvisible
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.textfield.TextInputLayout
import com.hoc.flowmvi.core_ui.clicks
import com.hoc.flowmvi.core_ui.firstChange
import com.hoc.flowmvi.core_ui.navigator.IntentProviders
import com.hoc.flowmvi.core_ui.textChanges
import com.hoc.flowmvi.core_ui.toast
import com.hoc.flowmvi.domain.model.UserValidationError
import com.hoc.flowmvi.mvi_base.AbstractMviActivity
import com.hoc.flowmvi.ui.add.databinding.ActivityAddBinding
import com.hoc081098.flowext.mapTo
import com.hoc081098.viewbindingdelegate.viewBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import timber.log.Timber

@ExperimentalCoroutinesApi
class AddActivity :
  AbstractMviActivity<ViewIntent, ViewState, SingleEvent, AddVM>(R.layout.activity_add) {
  override val vm by stateViewModel<AddVM>()
  private val addBinding by viewBinding<ActivityAddBinding>()

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> true.also { finish() }
      else -> super.onOptionsItemSelected(item)
    }
  }

  override fun handleSingleEvent(event: SingleEvent) {
    Timber.d("Event=$event")

    return when (event) {
      is SingleEvent.AddUserSuccess -> {
        toast("Add success")
        finish()
      }
      is SingleEvent.AddUserFailure -> {
        toast("Add failure")
      }
    }
  }

  override fun render(viewState: ViewState) {
    Timber.d("viewState=$viewState")

    addBinding.emailEditText.setErrorIfChanged(viewState.emailChanged) {
      if (UserValidationError.INVALID_EMAIL_ADDRESS in viewState.errors) "Invalid email"
      else null
    }
    addBinding.firstNameEditText.setErrorIfChanged(viewState.firstNameChanged) {
      if (UserValidationError.TOO_SHORT_FIRST_NAME in viewState.errors) "Too short first name"
      else null
    }
    addBinding.lastNameEditText.setErrorIfChanged(viewState.lastNameChanged) {
      if (UserValidationError.TOO_SHORT_LAST_NAME in viewState.errors) "Too short last name"
      else null
    }

    TransitionManager.endTransitions(addBinding.root)
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

  override fun setupViews() {
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    val state = vm.viewState.value

    addBinding.run {
      emailEditText.editText!!.setText(state.email)
      firstNameEditText.editText!!.setText(state.firstName)
      lastNameEditText.editText!!.setText(state.lastName)
    }
  }

  override fun viewIntents(): Flow<ViewIntent> = addBinding.run {
    merge(
      emailEditText
        .editText!!
        .textChanges()
        .map { ViewIntent.EmailChanged(it?.toString().orEmpty()) },
      firstNameEditText
        .editText!!
        .textChanges()
        .map { ViewIntent.FirstNameChanged(it?.toString().orEmpty()) },
      lastNameEditText
        .editText!!
        .textChanges()
        .map { ViewIntent.LastNameChanged(it?.toString().orEmpty()) },
      addButton
        .clicks()
        .map { ViewIntent.Submit },
      emailEditText
        .editText!!
        .firstChange()
        .mapTo(ViewIntent.EmailChangedFirstTime),
      firstNameEditText
        .editText!!
        .firstChange()
        .mapTo(ViewIntent.FirstNameChangedFirstTime),
      lastNameEditText
        .editText!!
        .firstChange()
        .mapTo(ViewIntent.LastNameChangedFirstTime),
    )
  }

  internal class IntentProvider : IntentProviders.Add {
    override fun makeIntent(context: Context): Intent =
      Intent(context, AddActivity::class.java)
  }
}

private inline fun TextInputLayout.setErrorIfChanged(
  errorEnabled: Boolean,
  errorBuilder: () -> String?,
) {
  val newError = if (errorEnabled) errorBuilder() else null
  // avoid unnecessary animation
  if (error != newError) {
    error = newError
  }
}
