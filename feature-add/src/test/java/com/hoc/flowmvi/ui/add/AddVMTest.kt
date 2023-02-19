package com.hoc.flowmvi.ui.add

import androidx.lifecycle.SavedStateHandle
import arrow.core.left
import arrow.core.right
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.model.UserValidationError
import com.hoc.flowmvi.domain.model.UserValidationError.TOO_SHORT_FIRST_NAME
import com.hoc.flowmvi.domain.model.UserValidationError.TOO_SHORT_LAST_NAME
import com.hoc.flowmvi.domain.usecase.AddUserUseCase
import com.hoc.flowmvi.mvi_testing.BaseMviViewModelTest
import com.hoc.flowmvi.mvi_testing.mapRight
import com.hoc.flowmvi.mvi_testing.returnsWithDelay
import com.hoc.flowmvi.test_utils.rightValueOrThrow
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf

private val ALL_ERRORS = UserValidationError.VALUES_SET
private const val EMAIL = "hoc081098@gmail.com"
private const val NAME = "hoc081098"

@ExperimentalCoroutinesApi
@ExperimentalTime
class AddVMTest : BaseMviViewModelTest<ViewIntent, ViewState, SingleEvent, AddVM>() {
  private lateinit var vm: AddVM
  private lateinit var addUser: AddUserUseCase
  private lateinit var savedStateHandle: SavedStateHandle

  override fun setup() {
    super.setup()

    addUser = mockk()
    savedStateHandle = SavedStateHandle()

    vm = AddVM(
      addUser = addUser,
      savedStateHandle = savedStateHandle,
      stateSaver = ViewState.StateSaver(),
    )
  }

  override fun tearDown() {
    confirmVerified(
      addUser,
    )

    super.tearDown()
  }

  @Test
  fun test_withFormValueIntents_returnsStateWithChangedValuesAndErrors() {
    runVMTest(
      vmProducer = { vm },
      intents = flowOf(
        ViewIntent.EmailChanged(""),
        ViewIntent.FirstNameChanged(""),
        ViewIntent.LastNameChanged(""),
        // all fields changed
        ViewIntent.EmailChanged("a"),
        ViewIntent.FirstNameChanged("b"),
        ViewIntent.LastNameChanged("c"),
      ),
      expectedStates = listOf(
        ViewState.initial(),
        ViewState(
          errors = ALL_ERRORS,
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "a",
          firstName = "",
          lastName = ""
        ),
        ViewState(
          errors = ALL_ERRORS,
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "a",
          firstName = "b",
          lastName = ""
        ),
        // invalid state
        ViewState(
          errors = ALL_ERRORS,
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "a",
          firstName = "b",
          lastName = "c"
        )
      ).mapRight(),
      expectedEvents = emptyList(),
    )
  }

  @Test
  fun test_withFormValueIntents_returnsStateWithChangedValuesAndNoErrors() {
    runVMTest(
      vmProducer = { vm },
      intents = flowOf(
        ViewIntent.EmailChanged(""),
        ViewIntent.FirstNameChanged(""),
        ViewIntent.LastNameChanged(""),
        // all fields changed
        ViewIntent.EmailChanged(EMAIL),
        ViewIntent.FirstNameChanged(NAME),
        ViewIntent.LastNameChanged(NAME),
      ),
      expectedStates = listOf(
        ViewState.initial(),
        // all fields changed.
        ViewState(
          errors = setOf(TOO_SHORT_FIRST_NAME, TOO_SHORT_LAST_NAME),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = EMAIL,
          firstName = "",
          lastName = ""
        ),
        ViewState(
          errors = setOf(TOO_SHORT_LAST_NAME),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = EMAIL,
          firstName = NAME,
          lastName = ""
        ),
        // valid state
        ViewState(
          errors = emptySet(),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = EMAIL,
          firstName = NAME,
          lastName = NAME
        ),
      ).mapRight(),
      expectedEvents = emptyList(),
    )
  }

  @Test
  fun test_withSubmitIntentWhenFormValidAndAddUserSuccess_callAddUserAndReturnsStateWithLoading() {
    val user = User.create(
      id = "",
      email = EMAIL,
      firstName = NAME,
      lastName = NAME,
      avatar = ""
    ).rightValueOrThrow

    coEvery { addUser(user) } returnsWithDelay Unit.right()

    runVMTest(
      vmProducer = { vm },
      intents = flowOf(ViewIntent.Submit),
      expectedStates = listOf(
        ViewState(
          errors = emptySet(),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = EMAIL,
          firstName = NAME,
          lastName = NAME,
        ),
        ViewState(
          errors = emptySet(),
          isLoading = true,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = EMAIL,
          firstName = NAME,
          lastName = NAME,
        ),
        ViewState(
          errors = emptySet(),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = EMAIL,
          firstName = NAME,
          lastName = NAME,
        ),
      ).mapRight(),
      expectedEvents = listOf(
        SingleEvent.AddUserSuccess(user),
      ).mapRight(),
      preProcessingIntents = flowOf(
        ViewIntent.EmailChanged(EMAIL),
        ViewIntent.FirstNameChanged(NAME),
        ViewIntent.LastNameChanged(NAME),
      ),
    ) {
      coVerify { addUser(user) }
    }
  }

  @Test
  fun test_withSubmitIntentWhenFormValidAndAddUserFailure_callAddUserAndReturnsStateWithLoading() {
    val user = User.create(
      id = "",
      email = EMAIL,
      firstName = NAME,
      lastName = NAME,
      avatar = ""
    ).rightValueOrThrow
    val networkError = UserError.NetworkError

    coEvery { addUser(user) } returnsWithDelay networkError.left()

    runVMTest(
      vmProducer = { vm },
      intents = flowOf(ViewIntent.Submit),
      expectedStates = listOf(
        ViewState(
          errors = emptySet(),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = EMAIL,
          firstName = NAME,
          lastName = NAME,
        ),
        ViewState(
          errors = emptySet(),
          isLoading = true,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = EMAIL,
          firstName = NAME,
          lastName = NAME,
        ),
        ViewState(
          errors = emptySet(),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = EMAIL,
          firstName = NAME,
          lastName = NAME,
        ),
      ).mapRight(),
      expectedEvents = listOf(
        SingleEvent.AddUserFailure(user = user, error = networkError),
      ).mapRight(),
      preProcessingIntents = flowOf(
        ViewIntent.EmailChanged(EMAIL),
        ViewIntent.FirstNameChanged(NAME),
        ViewIntent.LastNameChanged(NAME),
      ),
    ) {
      coVerify { addUser(user) }
    }
  }

  @Test
  fun test_withSubmitIntentWhenFormInvalid_doNothing() {
    runVMTest(
      vmProducer = { vm },
      intents = flowOf(ViewIntent.Submit),
      expectedStates = listOf(
        ViewState(
          errors = ALL_ERRORS,
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "",
          firstName = "",
          lastName = "",
        ),
      ).mapRight(),
      expectedEvents = emptyList(),
      preProcessingIntents = flowOf(
        ViewIntent.EmailChanged(""),
        ViewIntent.FirstNameChanged(""),
        ViewIntent.LastNameChanged(""),
      ),
    )
  }

  @Test
  fun test_withFirstChangeIntents_returnsStateWithFirstChangesSetToTrue() {
    runVMTest(
      vmProducer = { vm },
      intents = flowOf(
        ViewIntent.EmailChangedFirstTime,
        ViewIntent.FirstNameChangedFirstTime,
        ViewIntent.LastNameChangedFirstTime,
      ),
      expectedStates = listOf(
        ViewState.initial(),
        ViewState(
          errors = ALL_ERRORS,
          isLoading = false,
          emailChanged = true,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "",
          firstName = "",
          lastName = ""
        ),
        ViewState(
          errors = ALL_ERRORS,
          isLoading = false,
          emailChanged = true,
          firstNameChanged = true,
          lastNameChanged = false,
          email = "",
          firstName = "",
          lastName = ""
        ),
        ViewState(
          errors = ALL_ERRORS,
          isLoading = false,
          emailChanged = true,
          firstNameChanged = true,
          lastNameChanged = true,
          email = "",
          firstName = "",
          lastName = ""
        )
      ).mapRight(),
      expectedEvents = emptyList()
    )
  }
}
