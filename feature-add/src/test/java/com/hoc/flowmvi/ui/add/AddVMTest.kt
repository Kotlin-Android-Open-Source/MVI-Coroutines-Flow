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
import com.hoc.flowmvi.test_utils.valueOrThrow
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private val ALL_ERRORS = UserValidationError.values().toSet()
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
      savedStateHandle = savedStateHandle
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
    test(
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
          errors = emptySet(),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "",
          firstName = null,
          lastName = null
        ),
        ViewState(
          errors = emptySet(),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "",
          firstName = "",
          lastName = null
        ),
        ViewState(
          errors = emptySet(),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "",
          firstName = "",
          lastName = ""
        ),
        ViewState(
          errors = ALL_ERRORS,
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "",
          firstName = "",
          lastName = ""
        ),
        // all fields changed.
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
    test(
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
        ViewState(
          errors = emptySet(),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "",
          firstName = null,
          lastName = null
        ),
        ViewState(
          errors = emptySet(),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "",
          firstName = "",
          lastName = null
        ),
        ViewState(
          errors = emptySet(),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "",
          firstName = "",
          lastName = ""
        ),
        ViewState(
          errors = ALL_ERRORS,
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "",
          firstName = "",
          lastName = ""
        ),
        // all fields changed.
        ViewState(
          errors = ALL_ERRORS,
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = EMAIL,
          firstName = "",
          lastName = ""
        ),
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
          errors = setOf(TOO_SHORT_FIRST_NAME, TOO_SHORT_LAST_NAME),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = EMAIL,
          firstName = NAME,
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
        ViewState(
          errors = setOf(TOO_SHORT_LAST_NAME),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = EMAIL,
          firstName = NAME,
          lastName = NAME
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
    ).valueOrThrow

    coEvery { addUser(user) } returns Unit.right()

    test(
      vmProducer = { vm },
      intents = flowOf(ViewIntent.Submit),
      intentsBeforeCollecting = flowOf(
        ViewIntent.EmailChanged(EMAIL),
        ViewIntent.FirstNameChanged(NAME),
        ViewIntent.LastNameChanged(NAME),
      ),
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
      delayAfterDispatchingIntents = 1.seconds,
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
    ).valueOrThrow
    val networkError = UserError.NetworkError

    coEvery { addUser(user) } returns networkError.left()

    test(
      vmProducer = { vm },
      intents = flowOf(ViewIntent.Submit),
      intentsBeforeCollecting = flowOf(
        ViewIntent.EmailChanged(EMAIL),
        ViewIntent.FirstNameChanged(NAME),
        ViewIntent.LastNameChanged(NAME),
      ),
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
      delayAfterDispatchingIntents = 1.seconds,
    ) {
      coVerify { addUser(user) }
    }
  }

  @Test
  fun test_withSubmitIntentWhenFormInvalid_doNothing() {
    test(
      vmProducer = { vm },
      intents = flowOf(ViewIntent.Submit),
      intentsBeforeCollecting = flowOf(
        ViewIntent.EmailChanged(""),
        ViewIntent.FirstNameChanged(""),
        ViewIntent.LastNameChanged(""),
      ),
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
      delayAfterDispatchingIntents = 1.seconds,
    )
  }

  @Test
  fun test_withFirstChangeIntents_returnsStateWithFirstChangesSetToTrue() {
    test(
      vmProducer = { vm },
      intents = flowOf(
        ViewIntent.EmailChangedFirstTime,
        ViewIntent.FirstNameChangedFirstTime,
        ViewIntent.LastNameChangedFirstTime,
      ),
      expectedStates = listOf(
        ViewState.initial(),
        ViewState(
          errors = setOf(),
          isLoading = false,
          emailChanged = true,
          firstNameChanged = false,
          lastNameChanged = false,
          email = null,
          firstName = null,
          lastName = null
        ),
        ViewState(
          errors = setOf(),
          isLoading = false,
          emailChanged = true,
          firstNameChanged = true,
          lastNameChanged = false,
          email = null,
          firstName = null,
          lastName = null
        ),
        ViewState(
          errors = setOf(),
          isLoading = false,
          emailChanged = true,
          firstNameChanged = true,
          lastNameChanged = true,
          email = null,
          firstName = null,
          lastName = null
        )
      ).mapRight(),
      expectedEvents = emptyList()
    )
  }
}
