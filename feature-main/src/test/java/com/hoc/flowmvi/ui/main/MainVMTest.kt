package com.hoc.flowmvi.ui.main

import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

val users = listOf(
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

internal val usersItems = users.map { UserItem(it) }

@ExperimentalCoroutinesApi
@FlowPreview
class MainVMTest {
  private val testDispatcher = TestCoroutineDispatcher()

  private lateinit var vm: MainVM
  private val getUserUseCase: GetUsersUseCase = mockk(relaxed = true)
  private val refreshGetUsersUseCase: RefreshGetUsersUseCase = mockk(relaxed = true)
  private val removeUser: RemoveUserUseCase = mockk(relaxed = true)

  @BeforeTest
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    vm = MainVM(
      getUsersUseCase = getUserUseCase,
      refreshGetUsers = refreshGetUsersUseCase,
      removeUser = removeUser,
    )
  }

  @AfterTest
  fun teardown() {
    Dispatchers.resetMain()
    clearAllMocks()
  }

  @Test
  fun `ViewIntent_Initial returns success`() = testDispatcher.runBlockingTest {
    every { getUserUseCase() } returns flow {
      delay(100)
      emit(users)
    }

    launch(start = CoroutineStart.UNDISPATCHED) {
      vm.viewState
        .take(2)
        .toList()
        .let {
          assertContentEquals(
            it,
            listOf(
              ViewState.initial(),
              ViewState(
                userItems = usersItems,
                isLoading = false,
                error = null,
                isRefreshing = false
              )
            )
          )
        }
    }

    vm.processIntent(ViewIntent.Initial)
  }
}
