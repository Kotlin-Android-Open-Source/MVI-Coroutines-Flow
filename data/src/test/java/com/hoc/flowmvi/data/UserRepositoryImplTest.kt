package com.hoc.flowmvi.data

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.identity
import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.core.dispatchers.CoroutineDispatchers
import com.hoc.flowmvi.data.remote.UserApiService
import com.hoc.flowmvi.data.remote.UserBody
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.repository.UserError
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

private val USER_BODY = UserBody(
  email = "email1@gmail.com",
  firstName = "first",
  lastName = "last",
)

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
class TestDispatchersImpl(testDispatcher: TestCoroutineDispatcher) : CoroutineDispatchers {
  override val main: CoroutineDispatcher = testDispatcher
  override val io: CoroutineDispatcher = testDispatcher
}

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
      dispatchers = TestDispatchersImpl(testDispatcher),
      responseToDomain = responseToDomain,
      domainToBody = domainToBody,
      errorMapper = errorMapper
    )
  }

  @AfterTest
  fun tearDown() {
    testDispatcher.cleanupTestCoroutines()
    Dispatchers.resetMain()

    confirmVerified(
      userApiService,
      responseToDomain,
      domainToBody,
      errorMapper,
    )
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
    assertEquals(UserError.NetworkError, result.leftOrThrow)
    coVerify(exactly = 3) { userApiService.getUsers() } // retry 3 times
    verify(exactly = 1) { errorMapper(ofType<IOException>()) }
  }

  @Test
  fun test_remove_withApiCallSuccess_returnsRight() = testDispatcher.runBlockingTest {
    val user = USERS[0]
    val userResponse = USER_RESPONSES[0]

    coEvery { userApiService.remove(user.id) } returns userResponse
    every { responseToDomain(userResponse) } returns user

    val result = repo.remove(user)

    assertTrue(result.isRight())
    assertNotNull(result.orNull())

    coVerify { userApiService.remove(user.id) }
    coVerify { responseToDomain(userResponse) }
  }

  @Test
  fun test_remove_withApiCallError_returnsLeft() = testDispatcher.runBlockingTest {
    val user = USERS[0]
    coEvery { userApiService.remove(user.id) } throws IOException()
    every { errorMapper(ofType<IOException>()) } returns UserError.NetworkError

    val result = repo.remove(user)

    assertTrue(result.isLeft())
    assertEquals(UserError.NetworkError, result.leftOrThrow)
    coVerify(exactly = 1) { userApiService.remove(user.id) }
    verify(exactly = 1) { errorMapper(ofType<IOException>()) }
  }

  @Test
  fun test_add_withApiCallSuccess_returnsRight() = testDispatcher.runBlockingTest {
    val user = USERS[0]
    val userResponse = USER_RESPONSES[0]

    coEvery { userApiService.add(USER_BODY) } returns userResponse
    every { domainToBody(user) } returns USER_BODY
    every { responseToDomain(userResponse) } returns user

    val result = repo.add(user)

    assertTrue(result.isRight())
    assertNotNull(result.orNull())

    coVerify { userApiService.add(USER_BODY) }
    verify { domainToBody(user) }
    coVerify { responseToDomain(userResponse) }
  }

  @Test
  fun test_add_withApiCallError_returnsLeft() = testDispatcher.runBlockingTest {
    val user = USERS[0]
    coEvery { userApiService.add(USER_BODY) } throws IOException()
    every { domainToBody(user) } returns USER_BODY
    every { errorMapper(ofType<IOException>()) } returns UserError.NetworkError

    val result = repo.add(user)

    assertTrue(result.isLeft())
    assertEquals(UserError.NetworkError, result.leftOrThrow)

    coVerify(exactly = 1) { userApiService.add(USER_BODY) }
    verify(exactly = 1) { domainToBody(user) }
    verify(exactly = 1) { errorMapper(ofType<IOException>()) }
  }

  @Test
  fun test_search_withApiCallSuccess_returnsRight() = testDispatcher.runBlockingTest {
    val q = "hoc081098"
    coEvery { userApiService.search(q) } returns USER_RESPONSES
    every { responseToDomain(any()) } returnsMany USERS

    val result = repo.search(q)

    assertTrue(result.isRight())
    assertNotNull(result.orNull())
    assertContentEquals(
      USERS,
      result.getOrHandle { error("$result - $it - Should not reach here!") },
    )

    coVerify { userApiService.search(q) }
    coVerifySequence {
      USER_RESPONSES.forEach {
        responseToDomain(it)
      }
    }
  }

  @Test
  fun test_search_withApiCallError_returnsLeft() = testDispatcher.runBlockingTest {
    val q = "hoc081098"
    coEvery { userApiService.search(q) } throws IOException()
    every { errorMapper(ofType<IOException>()) } returns UserError.NetworkError

    val result = repo.search(q)

    assertTrue(result.isLeft())
    assertEquals(UserError.NetworkError, result.leftOrThrow)

    coVerify(exactly = 1) { userApiService.search(q) }
    verify(exactly = 1) { errorMapper(ofType<IOException>()) }
  }
}

private inline val <L, R> Either<L, R>.leftOrThrow: L
  get() = fold(::identity) {
    if (it is Throwable) throw it
    else error("$this - $it - Should not reach here!")
  }
