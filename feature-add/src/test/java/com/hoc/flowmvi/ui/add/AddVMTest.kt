package com.hoc.flowmvi.ui.add

import androidx.lifecycle.SavedStateHandle
import com.flowmvi.mvi_testing.BaseMviViewModelTest
import com.flowmvi.mvi_testing.mapRight
import com.hoc.flowmvi.domain.usecase.AddUserUseCase
import com.hoc.flowmvi.ui.add.ValidationError.TOO_SHORT_FIRST_NAME
import com.hoc.flowmvi.ui.add.ValidationError.TOO_SHORT_LAST_NAME
import com.hoc.flowmvi.ui.add.ValidationError.values
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.time.ExperimentalTime

private val ALL_ERRORS = values().toSet()

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
        ViewIntent.EmailChanged("hoc081098@gmail.com"),
        ViewIntent.FirstNameChanged("hoc081098"),
        ViewIntent.LastNameChanged("hoc081098"),
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
          email = "hoc081098@gmail.com",
          firstName = "",
          lastName = ""
        ),
        ViewState(
          errors = setOf(TOO_SHORT_FIRST_NAME, TOO_SHORT_LAST_NAME),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "hoc081098@gmail.com",
          firstName = "",
          lastName = ""
        ),
        ViewState(
          errors = setOf(TOO_SHORT_FIRST_NAME, TOO_SHORT_LAST_NAME),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "hoc081098@gmail.com",
          firstName = "hoc081098",
          lastName = ""
        ),
        ViewState(
          errors = setOf(TOO_SHORT_LAST_NAME),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "hoc081098@gmail.com",
          firstName = "hoc081098",
          lastName = ""
        ),
        ViewState(
          errors = setOf(TOO_SHORT_LAST_NAME),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "hoc081098@gmail.com",
          firstName = "hoc081098",
          lastName = "hoc081098"
        ),
        // valid state
        ViewState(
          errors = emptySet(),
          isLoading = false,
          emailChanged = false,
          firstNameChanged = false,
          lastNameChanged = false,
          email = "hoc081098@gmail.com",
          firstName = "hoc081098",
          lastName = "hoc081098"
        ),
      ).mapRight(),
      expectedEvents = emptyList(),
    )
  }
}
