package com.hoc.flowmvi.data

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.identity
import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.data.remote.UserApiService
import com.hoc.flowmvi.data.remote.UserBody
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.repository.UserError
import com.hoc.flowmvi.test_utils.TestCoroutineDispatcherRule
import com.hoc.flowmvi.test_utils.TestDispatchers
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
@ExperimentalTime
class UserRepositoryImplTest {
  @get:Rule
  val coroutineRule = TestCoroutineDispatcherRule()
  private val testDispatcher get() = coroutineRule.testCoroutineDispatcher

  private lateinit var repo: UserRepositoryImpl
  private lateinit var userApiService: UserApiService
  private lateinit var responseToDomain: Mapper<UserResponse, User>
  private lateinit var domainToBody: Mapper<User, UserBody>
  private lateinit var errorMapper: Mapper<Throwable, UserError>

  @BeforeTest
  fun setup() {
    userApiService = mockk()
    responseToDomain = mockk()
    domainToBody = mockk()
    errorMapper = mockk()

    repo = UserRepositoryImpl(
      userApiService = userApiService,
      dispatchers = TestDispatchers(coroutineRule.testCoroutineDispatcher),
      responseToDomain = responseToDomain,
      domainToBody = domainToBody,
      errorMapper = errorMapper
    )
  }

  @AfterTest
  fun tearDown() {
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
    assertContentEquals(USERS, result.getOrThrow)

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

  @Test
  fun test_getUsers_withApiCallSuccess_emitsInitial() = testDispatcher.runBlockingTest {
    coEvery { userApiService.getUsers() } returns USER_RESPONSES
    every { responseToDomain(any()) } returnsMany USERS

    val events = mutableListOf<Either<UserError, List<User>>>()
    val job = launch(start = CoroutineStart.UNDISPATCHED) {
      repo.getUsers().toList(events)
    }
    delay(5_000)
    job.cancel()

    assertEquals(1, events.size)
    val result = events.single()
    assertTrue(result.isRight())
    assertNotNull(result.orNull())
    assertEquals(USERS, result.getOrThrow)

    coVerify { userApiService.getUsers() }
    verifySequence {
      USER_RESPONSES.forEach {
        responseToDomain(it)
      }
    }
  }

  @Test
  fun test_getUsers_withApiCallError_rethrows() = testDispatcher.runBlockingTest {
    coEvery { userApiService.getUsers() } throws IOException()
    every { errorMapper(ofType<IOException>()) } returns UserError.NetworkError

    val events = mutableListOf<Either<UserError, List<User>>>()
    val job = launch(start = CoroutineStart.UNDISPATCHED) {
      repo.getUsers().toList(events)
    }
    delay(20_000)
    job.cancel()

    assertEquals(1, events.size)
    val result = events.single()
    assertTrue(result.isLeft())
    assertNull(result.orNull())
    assertEquals(UserError.NetworkError, result.leftOrThrow)

    coVerify(exactly = 3) { userApiService.getUsers() } // retry 3 times.
    verify(exactly = 1) { errorMapper(ofType<IOException>()) }
  }

  @Test
  fun test_getUsers_withApiCallSuccess_emitsInitialAndUpdatedUsers() =
    testDispatcher.runBlockingTest {
      val user = USERS.last()
      val userResponse = USER_RESPONSES.last()
      coEvery { userApiService.getUsers() } returns USER_RESPONSES.dropLast(1)
      coEvery { userApiService.add(USER_BODY) } returns userResponse
      coEvery { userApiService.remove(user.id) } returns userResponse
      every { domainToBody(user) } returns USER_BODY
      USER_RESPONSES.zip(USERS).forEach { (r, u) -> every { responseToDomain(r) } returns u }

      val events = mutableListOf<Either<UserError, List<User>>>()
      val job = launch(start = CoroutineStart.UNDISPATCHED) {
        repo.getUsers().toList(events)
      }
      repo.add(user)
      repo.remove(user)
      delay(120_000)
      job.cancel()

      assertContentEquals(
        events.map { it.getOrThrow },
        listOf(
          USERS.dropLast(1),
          USERS,
          USERS.dropLast(1),
        )
      )

      coVerify { userApiService.getUsers() }
      coVerify { userApiService.add(USER_BODY) }
      coVerify { userApiService.remove(user.id) }
      verify { domainToBody(user) }
      verifySequence {
        USER_RESPONSES.forEach { responseToDomain(it) }
        responseToDomain(USER_RESPONSES.last())
      }
    }
}

private inline val <L, R> Either<L, R>.leftOrThrow: L
  get() = fold(::identity) {
    if (it is Throwable) throw it
    else error("$this - $it - Should not reach here!")
  }

private inline val <L, R> Either<L, R>.getOrThrow: R
  get() = getOrHandle { error("$this - $it - Should not reach here!") }
