package com.hoc.flowmvi.data

import arrow.core.identity
import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.core.dispatchers.ImmediateDispatchersImpl
import com.hoc.flowmvi.data.remote.UserApiService
import com.hoc.flowmvi.data.remote.UserBody
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.repository.UserError
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

private val USER_RESPONSES = listOf(
  UserResponse(
    id = "1",
    email = "email1@gmail.com",
    firstName = "first",
    lastName = "last",
    avatar = "avatar1",
  ),
  UserResponse(
    id = "2",
    email = "email2@gmail.com",
    firstName = "first",
    lastName = "last",
    avatar = "avatar2",
  ),
  UserResponse(
    id = "3",
    email = "email3@gmail.com",
    firstName = "first",
    lastName = "last",
    avatar = "avatar3",
  ),
)

private val USERS = listOf(
  User(
    id = "1",
    email = "email1@gmail.com",
    firstName = "first",
    lastName = "last",
    avatar = "avatar1",
  ),
  User(
    id = "2",
    email = "email2@gmail.com",
    firstName = "first",
    lastName = "last",
    avatar = "avatar2",
  ),
  User(
    id = "3",
    email = "email3@gmail.com",
    firstName = "first",
    lastName = "last",
    avatar = "avatar3",
  ),
)

@ExperimentalCoroutinesApi
@ExperimentalTime
class UserRepositoryImplTest {
  private val testDispatcher = TestCoroutineDispatcher()

  private lateinit var repo: UserRepositoryImpl
  private lateinit var userApiService: UserApiService
  private lateinit var responseToDomain: Mapper<UserResponse, User>
  private lateinit var domainToBody: Mapper<User, UserBody>
  private lateinit var errorMapper: Mapper<Throwable, UserError>

  @BeforeTest
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    userApiService = mockk()
    responseToDomain = mockk()
    domainToBody = mockk()
    errorMapper = mockk()

    repo = UserRepositoryImpl(
      userApiService = userApiService,
      dispatchers = ImmediateDispatchersImpl(),
      responseToDomain = responseToDomain,
      domainToBody = domainToBody,
      errorMapper = errorMapper
    )
  }

  @AfterTest
  fun tearDown() {
    testDispatcher.cleanupTestCoroutines()
    Dispatchers.resetMain()
    clearAllMocks()
  }

  @Test
  fun test_refresh_withApiCallSuccess_returnsRight() = testDispatcher.runBlockingTest {
    coEvery { userApiService.getUsers() } returns USER_RESPONSES
    every { responseToDomain(any()) } returnsMany USERS

    val result = repo.refresh()

    assertTrue(result.isRight())
    assertNotNull(result.orNull())

    coVerify { userApiService.getUsers() }
    verifySequence {
      USER_RESPONSES.forEach {
        responseToDomain(it)
      }
    }
  }

  @Test
  fun test_refresh_withApiCallError_returnsLeft() = testDispatcher.runBlockingTest {
    val ioException = IOException()
    coEvery { userApiService.getUsers() } throws ioException
    every { errorMapper(ofType<IOException>()) } returns UserError.NetworkError

    val result = repo.refresh()

    assertTrue(result.isLeft())
    assertEquals(
      UserError.NetworkError,
      result.fold(::identity) { error("Should not reach here!") }
    )
    coVerify(exactly = 3) { userApiService.getUsers() } // retry 3 times
    verify(exactly = 1) { errorMapper(ofType<IOException>()) }
    confirmVerified(
      userApiService,
      errorMapper,
    )
    cancel()
  }
}
