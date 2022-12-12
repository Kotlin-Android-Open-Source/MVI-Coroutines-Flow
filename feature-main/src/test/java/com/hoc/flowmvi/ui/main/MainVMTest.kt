package com.hoc.flowmvi.ui.main

import arrow.core.left
import arrow.core.right
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import com.hoc.flowmvi.mvi_testing.BaseMviViewModelTest
import com.hoc.flowmvi.mvi_testing.delayEach
import com.hoc.flowmvi.mvi_testing.mapRight
import com.hoc.flowmvi.mvi_testing.returnsWithDelay
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update

@ExperimentalTime
@ExperimentalCoroutinesApi
@FlowPreview
class MainVMTest : BaseMviViewModelTest<ViewIntent,
  ViewState,
  SingleEvent,
  MainVM>() {
  private lateinit var vm: MainVM
  private lateinit var getUserUseCase: GetUsersUseCase
  private lateinit var refreshGetUsersUseCase: RefreshGetUsersUseCase
  private lateinit var removeUser: RemoveUserUseCase

  override fun setup() {
    super.setup()

    getUserUseCase = mockk()
    refreshGetUsersUseCase = mockk()
    removeUser = mockk()

    vm = MainVM(
      getUsersUseCase = getUserUseCase,
      refreshGetUsers = refreshGetUsersUseCase,
      removeUser = removeUser,
    )
  }

  override fun tearDown() {
    confirmVerified(
      getUserUseCase,
      refreshGetUsersUseCase,
      removeUser,
    )

    super.tearDown()
  }

  @Test
  fun test_withInitialIntentWhenSuccess_returnsUserItems() {
    runVMTest(
      vmProducer = {
        every { getUserUseCase() } returns flowOf(USERS.right())
        vm
      },
      intents = flowOf(ViewIntent.Initial),
      expectedStates = listOf(
        ViewState.initial(),
        ViewState(
          userItems = USER_ITEMS,
          isLoading = false,
          error = null,
          isRefreshing = false
        )
      ).mapRight(),
      expectedEvents = emptyList(),
    ) { verify(exactly = 1) { getUserUseCase() } }
  }

  @Test
  fun test_withInitialIntentWhenError_returnsErrorState() {
    val userError = UserError.NetworkError

    runVMTest(
      vmProducer = {
        every { getUserUseCase() } returns flowOf(userError.left())
        vm
      },
      intents = flowOf(ViewIntent.Initial),
      expectedStates = listOf(
        ViewState.initial(),
        ViewState(
          userItems = emptyList(),
          isLoading = false,
          error = userError,
          isRefreshing = false
        )
      ).mapRight(),
      expectedEvents = listOf(
        SingleEvent.GetUsersError(
          error = userError,
        ),
      ).mapRight(),
    ) { verify(exactly = 1) { getUserUseCase() } }
  }

  @Test
  fun test_withRefreshIntentWhenSuccess_isNotRefreshing() {
    runVMTest(
      vmProducer = {
        every { getUserUseCase() } returns flowOf(USERS.right()).delayEach()
        coEvery { refreshGetUsersUseCase() } returnsWithDelay Unit.right()
        vm
      },
      intents = flowOf(ViewIntent.Refresh),
      expectedStates = listOf(
        ViewState(
          userItems = USER_ITEMS,
          isLoading = false,
          error = null,
          isRefreshing = false
        ),
        ViewState(
          userItems = USER_ITEMS,
          isLoading = false,
          error = null,
          isRefreshing = true,
        ),
        ViewState(
          userItems = USER_ITEMS,
          isLoading = false,
          error = null,
          isRefreshing = false
        ),
      ).mapRight(),
      expectedEvents = listOf(
        SingleEvent.Refresh.Success
      ).mapRight(),
      preProcessingIntents = flowOf(ViewIntent.Initial),
    ) {
      coVerify(exactly = 1) { getUserUseCase() }
      coVerify(exactly = 1) { refreshGetUsersUseCase() }
    }
  }

  @Test
  fun test_withRefreshIntentWhenFailure_isNotRefreshing() {
    val userError = UserError.NetworkError

    runVMTest(
      vmProducer = {
        every { getUserUseCase() } returns flowOf(USERS.right()).delayEach()
        coEvery { refreshGetUsersUseCase() } returnsWithDelay userError.left()
        vm
      },
      intents = flowOf(ViewIntent.Refresh),
      expectedStates = listOf(
        ViewState(
          userItems = USER_ITEMS,
          isLoading = false,
          error = null,
          isRefreshing = false
        ),
        ViewState(
          userItems = USER_ITEMS,
          isLoading = false,
          error = null,
          isRefreshing = true,
        ),
        ViewState(
          userItems = USER_ITEMS,
          isLoading = false,
          error = null,
          isRefreshing = false
        ),
      ).mapRight(),
      expectedEvents = listOf(
        SingleEvent.Refresh.Failure(userError)
      ).mapRight(),
      preProcessingIntents = flowOf(ViewIntent.Initial),
    ) {
      coVerify(exactly = 1) { getUserUseCase() }
      coVerify(exactly = 1) { refreshGetUsersUseCase() }
    }
  }

  @Test
  fun test_withRefreshIntent_ignoredWhenIsLoading() {
    runVMTest(
      vmProducer = {
        coEvery { refreshGetUsersUseCase() } returns Unit.right()
        vm
      },
      intents = flowOf(ViewIntent.Refresh),
      expectedStates = listOf(ViewState.initial()).mapRight(),
      expectedEvents = emptyList(),
    ) { coVerify(exactly = 0) { refreshGetUsersUseCase() } }
  }

  @Test
  fun test_withRefreshIntent_ignoredWhenHavingError() {
    val userError = UserError.NetworkError

    runVMTest(
      vmProducer = {
        every { getUserUseCase() } returns flowOf(userError.left())
        coEvery { refreshGetUsersUseCase() } returns Unit.right()
        vm
      },
      intents = flowOf(ViewIntent.Refresh),
      expectedStates = listOf(
        ViewState(
          userItems = emptyList(),
          isLoading = false,
          error = userError,
          isRefreshing = false,
        )
      ).mapRight(),
      expectedEvents = emptyList(),
      preProcessingIntents = flowOf(ViewIntent.Initial),
    ) {
      coVerify(exactly = 1) { getUserUseCase() }
      coVerify(exactly = 0) { refreshGetUsersUseCase() }
    }
  }

  @Test
  fun test_withRetryIntent_ignoredWhenHavingNoError() {
    runVMTest(
      vmProducer = {
        every { getUserUseCase() } returns emptyFlow()
        vm
      },
      intents = flowOf(ViewIntent.Retry),
      expectedStates = listOf(ViewState.initial()).mapRight(),
      expectedEvents = emptyList(),
    ) { coVerify(exactly = 0) { getUserUseCase() } }
  }

  @Test
  fun test_withRetryIntentWhenSuccess_returnsUserItems() {
    val userError = UserError.NetworkError

    runVMTest(
      vmProducer = {
        every { getUserUseCase() } returnsMany listOf(
          flowOf(userError.left()).delayEach(),
          flowOf(USERS.right()).delayEach(),
        )
        vm
      },
      intents = flowOf(ViewIntent.Retry),
      expectedStates = listOf(
        ViewState(
          userItems = emptyList(),
          isLoading = false,
          error = userError,
          isRefreshing = false,
        ),
        ViewState(
          userItems = emptyList(),
          isLoading = true,
          error = null,
          isRefreshing = false,
        ),
        ViewState(
          userItems = USER_ITEMS,
          isLoading = false,
          error = null,
          isRefreshing = false,
        )
      ).mapRight(),
      expectedEvents = emptyList(),
      preProcessingIntents = flowOf(ViewIntent.Initial),
    ) { verify(exactly = 2) { getUserUseCase() } }
  }

  @Test
  fun test_withRetryIntentWhenSuccess_returnsErrorState() {
    val userError1 = UserError.NetworkError
    val userError2 = UserError.Unexpected

    runVMTest(
      vmProducer = {
        every { getUserUseCase() } returnsMany listOf(
          flowOf(userError1.left()).delayEach(),
          flowOf(userError2.left()).delayEach(),
        )
        vm
      },
      intents = flowOf(ViewIntent.Retry),
      expectedStates = listOf(
        ViewState(
          userItems = emptyList(),
          isLoading = false,
          error = userError1,
          isRefreshing = false,
        ),
        ViewState(
          userItems = emptyList(),
          isLoading = true,
          error = null,
          isRefreshing = false,
        ),
        ViewState(
          userItems = emptyList(),
          isLoading = false,
          error = userError2,
          isRefreshing = false,
        )
      ).mapRight(),
      expectedEvents = listOf(
        SingleEvent.GetUsersError(userError2),
      ).mapRight(),
      preProcessingIntents = flowOf(ViewIntent.Initial),
    ) { verify(exactly = 2) { getUserUseCase() } }
  }

  @Test
  fun test_withRemoveUserIntentWhenSuccess_returnsUserItemsExceptRemovedUserItems() {
    val user1 = USERS[0]
    val user2 = USERS[1]
    val item1 = USER_ITEMS[0]
    val item2 = USER_ITEMS[1]
    val usersFlow = MutableStateFlow(USERS.right())

    runVMTest(
      vmProducer = {
        every { getUserUseCase() } returns usersFlow
        coEvery { removeUser(any()) } coAnswers {
          usersFlow.update { either ->
            either.map { users ->
              users.filter { it.id != firstArg<User>().id }
            }
          }
          Unit.right()
        }
        vm
      },
      intents = flowOf(
        ViewIntent.RemoveUser(item1),
        ViewIntent.RemoveUser(item2),
      ),
      expectedStates = listOf(
        ViewState(
          userItems = USER_ITEMS,
          isLoading = false,
          error = null,
          isRefreshing = false,
        ),
        ViewState(
          userItems = USER_ITEMS.toMutableList().apply { remove(item1) },
          isLoading = false,
          error = null,
          isRefreshing = false,
        ),
        ViewState(
          userItems = USER_ITEMS.toMutableList().apply {
            remove(item1)
            remove(item2)
          },
          isLoading = false,
          error = null,
          isRefreshing = false,
        ),
      ).mapRight(),
      expectedEvents = listOf(
        SingleEvent.RemoveUser.Success(item1),
        SingleEvent.RemoveUser.Success(item2),
      ).mapRight(),
      preProcessingIntents = flowOf(ViewIntent.Initial)
    ) {
      coVerify(exactly = 1) { getUserUseCase() }
      coVerifySequence {
        removeUser(user1)
        removeUser(user2)
      }
    }
  }

  @Test
  fun test_withRemoveUserIntentWhenError_stateDoNotChange() {
    val user = USERS[0]
    val item = USER_ITEMS[0]
    val userError = UserError.NetworkError

    runVMTest(
      vmProducer = {
        every { getUserUseCase() } returns flowOf(USERS.right())
        coEvery { removeUser(any()) } returns userError.left()
        vm
      },
      intents = flowOf(ViewIntent.RemoveUser(item)),
      expectedStates = listOf(
        ViewState(
          userItems = USER_ITEMS,
          isLoading = false,
          error = null,
          isRefreshing = false,
        ),
      ).mapRight(),
      expectedEvents = listOf(
        { event: SingleEvent ->
          val removed = assertIs<SingleEvent.RemoveUser.Failure>(event)
          assertEquals(item, removed.user)
          assertEquals(userError, removed.error)
          assertEquals(removed.indexProducer(), 0)
        }.left(),
      ),
      preProcessingIntents = flowOf(ViewIntent.Initial)
    ) {
      coVerify(exactly = 1) { getUserUseCase() }
      coVerify(exactly = 1) { removeUser(user) }
    }
  }
}
