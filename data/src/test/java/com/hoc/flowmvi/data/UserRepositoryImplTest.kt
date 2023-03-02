package com.hoc.flowmvi.data

import arrow.core.Either
import com.hoc.flowmvi.core.EitherNes
import com.hoc.flowmvi.core.Mapper
import com.hoc.flowmvi.core.rightNes
import com.hoc.flowmvi.data.remote.UserApiService
import com.hoc.flowmvi.data.remote.UserBody
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.model.UserValidationError
import com.hoc.flowmvi.test_utils.TestAppCoroutineDispatchers
import com.hoc.flowmvi.test_utils.TestCoroutineDispatcherRule
import com.hoc.flowmvi.test_utils.leftValueOrThrow
import com.hoc.flowmvi.test_utils.rightValueOrThrow
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule

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
  User.create(
    id = "1",
    email = "email1@gmail.com",
    firstName = "first",
    lastName = "last",
    avatar = "avatar1",
  ),
  User.create(
    id = "2",
    email = "email2@gmail.com",
    firstName = "first",
    lastName = "last",
    avatar = "avatar2",
  ),
  User.create(
    id = "3",
    email = "email3@gmail.com",
    firstName = "first",
    lastName = "last",
    avatar = "avatar3",
  ),
).map { it.rightValueOrThrow }

private val VALID_NES_USERS = USERS.map(User::rightNes)

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalTime
class UserRepositoryImplTest {
  @get:Rule
  val coroutineRule = TestCoroutineDispatcherRule()

  private lateinit var repo: UserRepositoryImpl
  private lateinit var userApiService: UserApiService
  private lateinit var responseToDomain: Mapper<UserResponse, EitherNes<UserValidationError, User>>
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
      dispatchers = TestAppCoroutineDispatchers(coroutineRule.testDispatcher),
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
  fun test_refresh_withApiCallSuccess_returnsRight() = runTest {
    coEvery { userApiService.getUsers() } returns USER_RESPONSES
    every { responseToDomain(any()) } returnsMany VALID_NES_USERS

    val result = repo.refresh()

    assertTrue(result.isRight())
    assertNotNull(result.getOrNull())

    coVerify { userApiService.getUsers() }
    verifySequence {
      USER_RESPONSES.forEach {
        responseToDomain(it)
      }
    }
  }

  @Test
  fun test_refresh_withApiCallError_returnsLeft() = runTest {
    val ioException = IOException()
    coEvery { userApiService.getUsers() } throws ioException
    every { errorMapper(ofType<IOException>()) } returns UserError.NetworkError

    val result = repo.refresh()

    assertTrue(result.isLeft())
    assertEquals(UserError.NetworkError, result.leftValueOrThrow)
    coVerify(exactly = 3) { userApiService.getUsers() } // retry 2 times
    verify(exactly = 1) { errorMapper(ofType<IOException>()) }
  }

  @Test
  fun test_remove_withApiCallSuccess_returnsRight() = runTest {
    val user = USERS[0]
    val userResponse = USER_RESPONSES[0]

    coEvery { userApiService.remove(user.id) } returns userResponse
    every { responseToDomain(userResponse) } returns user.rightNes()

    val result = repo.remove(user)

    assertTrue(result.isRight())
    assertNotNull(result.getOrNull())

    coVerify { userApiService.remove(user.id) }
    coVerify { responseToDomain(userResponse) }
  }

  @Test
  fun test_remove_withApiCallError_returnsLeft() = runTest {
    val user = USERS[0]
    coEvery { userApiService.remove(user.id) } throws IOException()
    every { errorMapper(ofType<IOException>()) } returns UserError.NetworkError

    val result = repo.remove(user)

    assertTrue(result.isLeft())
    assertEquals(UserError.NetworkError, result.leftValueOrThrow)
    coVerify(exactly = 1) { userApiService.remove(user.id) }
    verify(exactly = 1) { errorMapper(ofType<IOException>()) }
  }

  @Test
  fun test_add_withApiCallSuccess_returnsRight() = runTest {
    val user = USERS[0]
    val userResponse = USER_RESPONSES[0]

    coEvery { userApiService.add(USER_BODY) } returns userResponse
    every { domainToBody(user) } returns USER_BODY
    every { responseToDomain(userResponse) } returns user.rightNes()

    val result = repo.add(user)

    assertTrue(result.isRight())
    assertNotNull(result.getOrNull())

    coVerify { userApiService.add(USER_BODY) }
    verify { domainToBody(user) }
    coVerify { responseToDomain(userResponse) }
  }

  @Test
  fun test_add_withApiCallError_returnsLeft() = runTest {
    val user = USERS[0]
    coEvery { userApiService.add(USER_BODY) } throws IOException()
    every { domainToBody(user) } returns USER_BODY
    every { errorMapper(ofType<IOException>()) } returns UserError.NetworkError

    val result = repo.add(user)

    assertTrue(result.isLeft())
    assertEquals(UserError.NetworkError, result.leftValueOrThrow)

    coVerify(exactly = 1) { userApiService.add(USER_BODY) }
    verify(exactly = 1) { domainToBody(user) }
    verify(exactly = 1) { errorMapper(ofType<IOException>()) }
  }

  @Test
  fun test_search_withApiCallSuccess_returnsRight() = runTest {
    val q = "hoc081098"
    coEvery { userApiService.search(q) } returns USER_RESPONSES
    every { responseToDomain(any()) } returnsMany VALID_NES_USERS

    val result = repo.search(q)

    assertTrue(result.isRight())
    assertNotNull(result.getOrNull())
    assertContentEquals(USERS, result.rightValueOrThrow)

    coVerify { userApiService.search(q) }
    coVerifySequence {
      USER_RESPONSES.forEach {
        responseToDomain(it)
      }
    }
  }

  @Test
  fun test_search_withApiCallError_returnsLeft() = runTest {
    val q = "hoc081098"
    coEvery { userApiService.search(q) } throws IOException()
    every { errorMapper(ofType<IOException>()) } returns UserError.NetworkError

    val result = repo.search(q)

    assertTrue(result.isLeft())
    assertEquals(UserError.NetworkError, result.leftValueOrThrow)

    coVerify(exactly = 1) { userApiService.search(q) }
    verify(exactly = 1) { errorMapper(ofType<IOException>()) }
  }

  @Test
  fun test_getUsers_withApiCallSuccess_emitsInitial() = runTest {
    coEvery { userApiService.getUsers() } returns USER_RESPONSES
    every { responseToDomain(any()) } returnsMany VALID_NES_USERS

    val events = mutableListOf<Either<UserError, List<User>>>()
    val job = launch(start = CoroutineStart.UNDISPATCHED) {
      repo.getUsers().toList(events)
    }
    delay(5_000)
    job.cancel()

    assertEquals(1, events.size)
    val result = events.single()
    assertTrue(result.isRight())
    assertNotNull(result.getOrNull())
    assertEquals(USERS, result.rightValueOrThrow)

    coVerify { userApiService.getUsers() }
    verifySequence {
      USER_RESPONSES.forEach {
        responseToDomain(it)
      }
    }
  }

  @Test
  fun test_getUsers_withApiCallError_rethrows() = runTest {
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
    assertNull(result.getOrNull())
    assertEquals(UserError.NetworkError, result.leftValueOrThrow)

    coVerify(exactly = 3) { userApiService.getUsers() } // retry 2 times.
    verify(exactly = 1) { errorMapper(ofType<IOException>()) }
  }

  @Test
  fun test_getUsers_withApiCallSuccess_emitsInitialAndUpdatedUsers() =
    runTest {
      val user = USERS.last()
      val userResponse = USER_RESPONSES.last()
      coEvery { userApiService.getUsers() } returns USER_RESPONSES.dropLast(1)
      coEvery { userApiService.add(USER_BODY) } returns userResponse
      coEvery { userApiService.remove(user.id) } returns userResponse
      every { domainToBody(user) } returns USER_BODY
      USER_RESPONSES.zip(USERS)
        .forEach { (r, u) -> every { responseToDomain(r) } returns u.rightNes() }

      val events = mutableListOf<Either<UserError, List<User>>>()
      val job = launch(start = CoroutineStart.UNDISPATCHED) {
        repo.getUsers().toList(events)
      }
      repo.add(user)
      repo.remove(user)
      delay(120_000)
      job.cancel()

      assertContentEquals(
        events.map { it.rightValueOrThrow },
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
