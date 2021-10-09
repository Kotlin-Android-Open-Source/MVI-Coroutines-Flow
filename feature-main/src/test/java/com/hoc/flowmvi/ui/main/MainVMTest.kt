package com.hoc.flowmvi.ui.main

import com.flowmvi.mvi_testing.BaseMviViewModelTest
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

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
  fun `ViewIntent_Initial returns success`() = test(
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
  fun `ViewIntent_Initial returns failure`() {
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
  fun `ViewIntent_Refresh returns success`() {
    test(
      vmProducer = { vm },
      intents = flowOf(ViewIntent.Refresh),
      expectedStates = listOf(ViewState.initial()),
      expectedEvents = emptyList(),
    )
  }
}
