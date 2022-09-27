package com.hoc.flowmvi.domain

import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.repository.UserRepository
import com.hoc.flowmvi.domain.usecase.AddUserUseCase
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import com.hoc.flowmvi.domain.usecase.SearchUsersUseCase
import com.hoc.flowmvi.test_utils.TestCoroutineDispatcherRule
import com.hoc.flowmvi.test_utils.thenShift
import com.hoc.flowmvi.test_utils.valueOrThrow
import com.hoc.flowmvi.test_utils.withAnyEffectScope
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule

private val USERS = listOf(
  User.create(
    id = "1",
    email = "email1@gmail.com",
    firstName = "first1",
    lastName = "last1",
    avatar = "1.png"
  ),
  User.create(
    id = "2",
    email = "email1@gmail.com",
    firstName = "first2",
    lastName = "last2",
    avatar = "2.png"
  ),
  User.create(
    id = "3",
    email = "email1@gmail.com",
    firstName = "first3",
    lastName = "last3",
    avatar = "3.png"
  ),
).map { it.valueOrThrow }

@ExperimentalCoroutinesApi
class UseCaseTest {
  @get:Rule
  val coroutineRule = TestCoroutineDispatcherRule()

  private lateinit var userRepository: UserRepository
  private lateinit var getUsersUseCase: GetUsersUseCase
  private lateinit var refreshUseCase: RefreshGetUsersUseCase
  private lateinit var removeUserUseCase: RemoveUserUseCase
  private lateinit var addUserUseCase: AddUserUseCase
  private lateinit var searchUsersUseCase: SearchUsersUseCase

  private val networkError = UserError.NetworkError
  private val errorLeft = networkError.left()

  @BeforeTest
  fun setup() {
    userRepository = mockk()

    getUsersUseCase = GetUsersUseCase(userRepository)
    refreshUseCase = RefreshGetUsersUseCase(userRepository)
    removeUserUseCase = RemoveUserUseCase(userRepository)
    addUserUseCase = AddUserUseCase(userRepository)
    searchUsersUseCase = SearchUsersUseCase(userRepository)
  }

  @AfterTest
  fun tearDown() {
    confirmVerified(userRepository)
    clearAllMocks()
  }

  @Test
  fun test_getUsersUseCase_whenSuccess_emitsUsers() = runTest {
    val usersRight = USERS.right()
    every { userRepository.getUsers() } returns flowOf(usersRight)

    val result = getUsersUseCase()

    verify { userRepository.getUsers() }
    assertEquals(usersRight, result.first())
  }

  @Test
  fun test_getUsersUseCase_whenError_throwsError() = runTest {
    every { userRepository.getUsers() } returns flowOf(errorLeft)

    val result = getUsersUseCase()

    verify { userRepository.getUsers() }
    assertEquals(errorLeft, result.first())
  }

  @Test
  fun test_refreshUseCase_whenSuccess_returnsUnit() = runTest {
    coEvery { withAnyEffectScope { userRepository.refresh() } } just Runs

    val result = either { refreshUseCase() }

    coVerify { withAnyEffectScope { userRepository.refresh() } }
    assertEquals(Unit.right(), result)
  }

  @Test
  fun test_refreshUseCase_whenError_throwsError() = runTest {
    coEvery { withAnyEffectScope { userRepository.refresh() } } thenShift networkError

    val result = either { refreshUseCase() }

    coVerify { withAnyEffectScope { userRepository.refresh() } }
    assertEquals(errorLeft, result)
  }

  @Test
  fun test_removeUserUseCase_whenSuccess_returnsUnit() = runTest {
    coEvery { withAnyEffectScope { userRepository.remove(any()) } } just Runs

    val result = either { removeUserUseCase(USERS[0]) }

    coVerify { withAnyEffectScope { userRepository.remove(USERS[0]) } }
    assertEquals(Unit.right(), result)
  }

  @Test
  fun test_removeUserUseCase_whenError_throwsError() = runTest {
    coEvery { withAnyEffectScope { userRepository.remove(any()) } } thenShift networkError

    val result = either { removeUserUseCase(USERS[0]) }

    coVerify { withAnyEffectScope { userRepository.remove(USERS[0]) } }
    assertEquals(errorLeft, result)
  }

  @Test
  fun test_addUserUseCase_whenSuccess_returnsUnit() = runTest {
    coEvery { withAnyEffectScope { userRepository.add(any()) } } just Runs

    val result = either { addUserUseCase(USERS[0]) }

    coVerify { withAnyEffectScope { userRepository.add(USERS[0]) } }
    assertEquals(Unit.right(), result)
  }

  @Test
  fun test_addUserUseCase_whenError_throwsError() = runTest {
    coEvery { withAnyEffectScope { userRepository.add(any()) } } thenShift networkError

    val result = either { addUserUseCase(USERS[0]) }

    coVerify { withAnyEffectScope { userRepository.add(USERS[0]) } }
    assertEquals(errorLeft, result)
  }

  @Test
  fun test_searchUsersUseCase_whenSuccess_returnsUsers() = runTest {
    coEvery { withAnyEffectScope { userRepository.search(any()) } } returns USERS

    val query = "hoc081098"
    val result = either { searchUsersUseCase(query) }

    coVerify { withAnyEffectScope { userRepository.search(query) } }
    assertEquals(USERS.right(), result)
  }

  @Test
  fun test_searchUsersUseCase_whenError_throwsError() = runTest {
    coEvery { withAnyEffectScope { userRepository.search(any()) } } thenShift networkError

    val query = "hoc081098"
    val result = either { searchUsersUseCase(query) }

    coVerify { withAnyEffectScope { userRepository.search(query) } }
    assertEquals(errorLeft, result)
  }
}
