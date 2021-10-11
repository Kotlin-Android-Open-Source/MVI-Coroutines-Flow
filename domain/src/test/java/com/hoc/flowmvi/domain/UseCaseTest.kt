package com.hoc.flowmvi.domain

import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.repository.UserRepository
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

@ExperimentalCoroutinesApi
class UseCaseTest {
  private val testDispatcher = TestCoroutineDispatcher()

  private val userRepository: UserRepository = mockk(relaxed = true)
  private val getUsersUseCase: GetUsersUseCase = GetUsersUseCase(userRepository)
  private val refreshUseCase: RefreshGetUsersUseCase = RefreshGetUsersUseCase(userRepository)
  private val removeUserUseCase: RemoveUserUseCase = RemoveUserUseCase(userRepository)

  @Test
  fun test_getUsersUseCase_whenSuccess_emitsUsers() = testDispatcher.runBlockingTest {
    every { userRepository.getUsers() } returns flowOf(USERS)

    val result = getUsersUseCase()

    verify { userRepository.getUsers() }
    assertEquals(USERS, result.first())
  }

  @Test
  fun test_getUsersUseCase_whenError_throwsError() = testDispatcher.runBlockingTest {
    every { userRepository.getUsers() } returns flow { throw Exception() }

    val result = getUsersUseCase()

    verify { userRepository.getUsers() }
    assertFailsWith<Exception> { result.first() }
  }

  @Test
  fun test_refreshUseCase_whenSuccess_returnsUnit() = testDispatcher.runBlockingTest {
    coEvery { userRepository.refresh() } returns Unit

    val result = refreshUseCase()

    coVerify { userRepository.refresh() }
    assertEquals(Unit, result)
  }

  @Test
  fun test_refreshUseCase_whenError_throwsError() = testDispatcher.runBlockingTest {
    coEvery { userRepository.refresh() } throws IOException()

    assertFailsWith<IOException> { refreshUseCase() }

    coVerify { userRepository.refresh() }
  }

  @Test
  fun test_removeUserUseCase_whenSuccess_returnsUnit() = testDispatcher.runBlockingTest {
    coEvery { userRepository.remove(any()) } returns Unit

    val result = removeUserUseCase(USERS[0])

    coVerify { userRepository.remove(USERS[0]) }
    assertEquals(Unit, result)
  }

  @Test
  fun test_removeUserUseCase_whenError_throwsError() = testDispatcher.runBlockingTest {
    coEvery { userRepository.remove(any()) } throws IOException()

    assertFailsWith<IOException> { removeUserUseCase(USERS[0]) }

    coVerify { userRepository.remove(USERS[0]) }
  }
}
