package com.hoc.flowmvi.ui.main

import com.flowmvi.mvi_testing.BaseMviViewModelTest
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import java.io.IOException
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

private val USERS = listOf(
  User(
    id = "1",
    email = "email1@gmail.com",
    firstName = "first1",
    lastName = "last1",
    avatar = "1.png"
  ),
  User(
    id = "2",
    email = "email1@gmail.com",
    firstName = "first2",
    lastName = "last2",
    avatar = "2.png"
  ),
  User(
    id = "3",
    email = "email1@gmail.com",
    firstName = "first3",
    lastName = "last3",
    avatar = "3.png"
  ),
)

private val USER_ITEMS = USERS.map(::UserItem)

@ExperimentalTime
@ExperimentalCoroutinesApi
@FlowPreview
class MainVMTest : BaseMviViewModelTest<
  ViewIntent,
  ViewState,
  SingleEvent,
  MainVM
  >() {
  private lateinit var vm: MainVM
  private val getUserUseCase: GetUsersUseCase = mockk(relaxed = true)
  private val refreshGetUsersUseCase: RefreshGetUsersUseCase = mockk(relaxed = true)
  private val removeUser: RemoveUserUseCase = mockk(relaxed = true)

  override fun setup() {
    super.setup()

    vm = MainVM(
      getUsersUseCase = getUserUseCase,
      refreshGetUsers = refreshGetUsersUseCase,
      removeUser = removeUser,
    )
    println("DONE setup $vm")
  }

  override fun tearDown() {
    super.tearDown()
    println("DONE tearDown")
  }

  @Test
  fun test_withInitialIntentWhenSuccess_returnsUserItems() = test(
    vmProducer = {
      every { getUserUseCase() } returns flowOf(USERS)
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
    ),
    expectedEvents = emptyList(),
  ) { verify(exactly = 1) { getUserUseCase() } }

  @Test
  fun test_withInitialIntentWhenError_returnsErrorState() {
    val ioException = IOException()

    test(
      vmProducer = {
        every { getUserUseCase() } returns flow { throw ioException }
        vm
      },
      intents = flowOf(ViewIntent.Initial),
      expectedStates = listOf(
        ViewState.initial(),
        ViewState(
          userItems = emptyList(),
          isLoading = false,
          error = ioException,
          isRefreshing = false
        )
      ),
      expectedEvents = listOf(
        SingleEvent.GetUsersError(
          error = ioException,
        ),
      ),
    ) { verify(exactly = 1) { getUserUseCase() } }
  }

  @Test
  fun test_withRefreshIntentWhenSuccess_isNotRefreshing() = test(
    vmProducer = {
      every { getUserUseCase() } returns flowOf(USERS)
      coEvery { refreshGetUsersUseCase() } returns Unit
      vm
    },
    intentsBeforeCollecting = flowOf(ViewIntent.Initial),
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
    ),
    expectedEvents = listOf(
      SingleEvent.Refresh.Success
    ),
  ) { coVerify(exactly = 1) { refreshGetUsersUseCase() } }

  @Test
  fun test_withRefreshIntentWhenFailure_isNotRefreshing() {
    val ioException = IOException()

    test(
      vmProducer = {
        coEvery { getUserUseCase() } returns flowOf(USERS)
        coEvery { refreshGetUsersUseCase() } throws ioException
        vm
      },
      intentsBeforeCollecting = flowOf(ViewIntent.Initial),
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
      ),
      expectedEvents = listOf(
        SingleEvent.Refresh.Failure(ioException)
      ),
    ) { coVerify(exactly = 1) { refreshGetUsersUseCase() } }
  }

  @Test
  fun test_withRefreshIntent_ignoredWhenIsLoading() {
    test(
      vmProducer = {
        coEvery { refreshGetUsersUseCase() } returns Unit
        vm
      },
      intents = flowOf(ViewIntent.Refresh),
      expectedStates = listOf(ViewState.initial()),
      expectedEvents = emptyList(),
      delayAfterDispatchingIntents = Duration.milliseconds(100),
    ) { coVerify(exactly = 0) { refreshGetUsersUseCase() } }
  }

  @Test
  fun test_withRefreshIntent_ignoredWhenHavingError() {
    val ioException = IOException()
    test(
      vmProducer = {
        every { getUserUseCase() } returns flow { throw ioException }
        coEvery { refreshGetUsersUseCase() } returns Unit
        vm
      },
      intentsBeforeCollecting = flowOf(ViewIntent.Initial),
      intents = flowOf(ViewIntent.Refresh),
      expectedStates = listOf(
        ViewState(
          userItems = emptyList(),
          isLoading = false,
          error = ioException,
          isRefreshing = false,
        )
      ),
      expectedEvents = listOf(
        SingleEvent.GetUsersError(ioException),
      ),
      delayAfterDispatchingIntents = Duration.milliseconds(100),
    ) { coVerify(exactly = 0) { refreshGetUsersUseCase() } }
  }

  @Test
  fun test_withRetryIntent_ignoredWhenHavingNoError() {
    test(
      vmProducer = {
        every { getUserUseCase() } returns emptyFlow()
        vm
      },
      intents = flowOf(ViewIntent.Retry),
      expectedStates = listOf(ViewState.initial()),
      expectedEvents = emptyList(),
      delayAfterDispatchingIntents = Duration.milliseconds(100),
    ) { coVerify(exactly = 0) { getUserUseCase() } }
  }

  @Test
  fun test_withRetryIntentWhenSuccess_returnsUserItems() {
    val ioException = IOException()
    test(
      vmProducer = {
        every { getUserUseCase() } returnsMany listOf(
          flow { throw ioException },
          flowOf(USERS),
        )
        vm
      },
      intentsBeforeCollecting = flowOf(ViewIntent.Initial),
      intents = flowOf(ViewIntent.Retry),
      expectedStates = listOf(
        ViewState(
          userItems = emptyList(),
          isLoading = false,
          error = ioException,
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
      ),
      expectedEvents = listOf(
        SingleEvent.GetUsersError(ioException),
      ),
    ) { verify(exactly = 2) { getUserUseCase() } }
  }

  @Test
  fun test_withRetryIntentWhenSuccess_returnsErrorState() {
    val ioException1 = IOException()
    val ioException2 = IOException()
    test(
      vmProducer = {
        every { getUserUseCase() } returnsMany listOf(
          flow { throw ioException1 },
          flow { throw ioException2 },
        )
        vm
      },
      intentsBeforeCollecting = flowOf(ViewIntent.Initial),
      intents = flowOf(ViewIntent.Retry),
      expectedStates = listOf(
        ViewState(
          userItems = emptyList(),
          isLoading = false,
          error = ioException1,
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
          error = ioException2,
          isRefreshing = false,
        )
      ),
      expectedEvents = listOf(
        SingleEvent.GetUsersError(ioException1),
        SingleEvent.GetUsersError(ioException2),
      ),
    ) { verify(exactly = 2) { getUserUseCase() } }
  }

  @Test
  fun test_withRemoveUserIntentWhenSuccess_returnsUserItemsExceptRemovedUserItems() {
    val user1 = USERS[0]
    val user2 = USERS[1]
    val item1 = USER_ITEMS[0]
    val item2 = USER_ITEMS[1]
    val usersFlow = MutableStateFlow(USERS)

    test(
      vmProducer = {
        every { getUserUseCase() } returns usersFlow
        coEvery { removeUser(any()) } coAnswers {
          usersFlow.update { users ->
            users.filter { it.id != firstArg<User>().id }
          }
        }
        vm
      },
      intentsBeforeCollecting = flowOf(ViewIntent.Initial),
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
      ),
      expectedEvents = listOf(
        SingleEvent.RemoveUser.Success(item1),
        SingleEvent.RemoveUser.Success(item2),
      )
    ) {
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
    val ioException = IOException()

    test(
      vmProducer = {
        every { getUserUseCase() } returns flowOf(USERS)
        coEvery { removeUser(any()) } throws ioException
        vm
      },
      intentsBeforeCollecting = flowOf(ViewIntent.Initial),
      intents = flowOf(ViewIntent.RemoveUser(item)),
      expectedStates = listOf(
        ViewState(
          userItems = USER_ITEMS,
          isLoading = false,
          error = null,
          isRefreshing = false,
        ),
      ),
      expectedEvents = listOf(
        SingleEvent.RemoveUser.Failure(item, ioException),
      )
    ) {
      coVerify(exactly = 1) { removeUser(user) }
    }
  }
}
