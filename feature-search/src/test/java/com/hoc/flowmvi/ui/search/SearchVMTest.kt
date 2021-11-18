package com.hoc.flowmvi.ui.search

import androidx.lifecycle.SavedStateHandle
import arrow.core.left
import arrow.core.right
import com.flowmvi.mvi_testing.BaseMviViewModelTest
import com.flowmvi.mvi_testing.mapRight
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.usecase.SearchUsersUseCase
import com.hoc.flowmvi.ui.search.SearchVM.Companion.SEARCH_DEBOUNCE_DURATION
import com.hoc081098.flowext.concatWith
import com.hoc081098.flowext.timer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalTime
class SearchVMTest : BaseMviViewModelTest<ViewIntent, ViewState, SingleEvent, SearchVM>() {
  private lateinit var vm: SearchVM
  private lateinit var savedStateHandle: SavedStateHandle
  private lateinit var searchUsersUseCase: SearchUsersUseCase

  override fun setup() {
    super.setup()

    searchUsersUseCase = mockk()
    savedStateHandle = SavedStateHandle()
    vm = SearchVM(
      searchUsersUseCase = searchUsersUseCase,
      savedStateHandle = savedStateHandle
    )
  }

  override fun tearDown() {
    confirmVerified(
      searchUsersUseCase,
    )
    super.tearDown()
  }

  @Test
  fun test_withSearchIntent_debounceSearchQuery() {
    val query = "d"
    coEvery { searchUsersUseCase(query) } returns USERS.right()

    test(
      vmProducer = { vm },
      intents = flowOf("a", "b", "c", query)
        .map { ViewIntent.Search(it) }
        .onEach { delay(SEMI_TIMEOUT) }
        .onCompletion { timeout() },
      expectedStates = listOf(
        ViewState.initial(null),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = "a", // update originalQuery
        ),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = "b", // update originalQuery
        ),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = "c", // update originalQuery
        ),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = query, // update originalQuery
        ),
        ViewState(
          users = emptyList(),
          isLoading = true, // update isLoading
          error = null,
          submittedQuery = "",
          originalQuery = query,
        ),
        ViewState(
          users = USER_ITEMS, // update users
          isLoading = false, // update isLoading
          error = null,
          submittedQuery = query, // update submittedQuery
          originalQuery = query,
        ),
      ).mapRight(),
      expectedEvents = emptyList(),
      delayAfterDispatchingIntents = EXTRAS_TIMEOUT,
    ) {
      coVerify(exactly = 1) { searchUsersUseCase(query) }
    }
  }

  @Test
  fun test_withSearchIntent_debounceSearchQueryAndRejectBlank() {
    val query = "     "

    test(
      vmProducer = { vm },
      intents = flowOf("a", "b", "c", query)
        .map { ViewIntent.Search(it) }
        .onEach { delay(SEMI_TIMEOUT) }
        .onCompletion { timeout() },
      expectedStates = listOf(
        ViewState.initial(null),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = "a", // update originalQuery
        ),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = "b", // update originalQuery
        ),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = "c", // update originalQuery
        ),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = query, // update originalQuery
        ),
      ).mapRight(),
      expectedEvents = emptyList(),
      delayAfterDispatchingIntents = EXTRAS_TIMEOUT,
    )
  }

  @Test
  fun test_withSearchIntent_debounceSearchQueryThenRejectBlankAndDistinctUntilChanged() {
    val query = "#query"
    coEvery { searchUsersUseCase(query) } returns USERS.right()

    test(
      vmProducer = { vm },
      intents = flowOf("a", "b", "c", query)
        .map { ViewIntent.Search(it) }
        .onEach { delay(SEMI_TIMEOUT) }
        .onCompletion { timeout() }
        .concatWith(timer(ViewIntent.Search(query), TOTAL_TIMEOUT))
        .onCompletion { timeout() },
      expectedStates = listOf(
        ViewState.initial(null),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = "a", // update originalQuery
        ),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = "b", // update originalQuery
        ),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = "c", // update originalQuery
        ),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = query, // update originalQuery
        ),
        ViewState(
          users = emptyList(),
          isLoading = true, // update isLoading
          error = null,
          submittedQuery = "",
          originalQuery = query,
        ),
        ViewState(
          users = USER_ITEMS, // update users
          isLoading = false, // update isLoading
          error = null,
          submittedQuery = query, // update submittedQuery
          originalQuery = query,
        ),
      ).mapRight(),
      expectedEvents = emptyList(),
      delayAfterDispatchingIntents = EXTRAS_TIMEOUT,
    ) {
      coVerify(exactly = 1) { searchUsersUseCase(query) }
    }
  }

  @Test
  fun test_withSearchIntent_returnsUserItemsWithProperLoadingState() {
    val query = "query"
    coEvery { searchUsersUseCase(query) } returns USERS.right()

    test(
      vmProducer = { vm },
      intents = flow {
        emit(ViewIntent.Search(query))
        timeout()
      },
      expectedStates = listOf(
        ViewState.initial(null),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = query, // update originalQuery
        ),
        ViewState(
          users = emptyList(),
          isLoading = true, // update isLoading
          error = null,
          submittedQuery = "",
          originalQuery = query,
        ),
        ViewState(
          users = USER_ITEMS, // update users
          isLoading = false, // update isLoading
          error = null,
          submittedQuery = query, // update submittedQuery
          originalQuery = query,
        ),
      ).mapRight(),
      expectedEvents = emptyList(),
      delayAfterDispatchingIntents = EXTRAS_TIMEOUT,
    ) {
      coVerify(exactly = 1) { searchUsersUseCase(query) }
    }
  }

  @Test
  fun test_withSearchIntent_returnsUserErrorWithProperLoadingState() {
    val query = "query"
    val networkError = UserError.NetworkError
    coEvery { searchUsersUseCase(query) } returns networkError.left()

    test(
      vmProducer = { vm },
      intents = flow {
        emit(ViewIntent.Search(query))
        timeout()
      },
      expectedStates = listOf(
        ViewState.initial(null),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = query, // update originalQuery
        ),
        ViewState(
          users = emptyList(),
          isLoading = true, // update isLoading
          error = null,
          submittedQuery = "",
          originalQuery = query,
        ),
        ViewState(
          users = emptyList(),
          isLoading = false, // update isLoading
          error = networkError, // update error
          submittedQuery = query, // update submittedQuery
          originalQuery = query,
        ),
      ).mapRight(),
      expectedEvents = listOf(
        SingleEvent.SearchFailure(networkError)
      ).mapRight(),
      delayAfterDispatchingIntents = EXTRAS_TIMEOUT,
    ) {
      coVerify(exactly = 1) { searchUsersUseCase(query) }
    }
  }

  @Test
  fun test_withRetryIntentWhenNoError_ignored() {
    test(
      vmProducer = { vm },
      intents = flowOf(ViewIntent.Retry),
      expectedStates = listOf(
        ViewState.initial(null),
      ).mapRight(),
      expectedEvents = emptyList(),
    )
  }

  @Test
  fun test_withRetryIntentWhenError_returnsUserItemsWithProperLoadingState() {
    val query = "#hoc081098"
    val networkError = UserError.NetworkError
    coEvery { searchUsersUseCase(query) } returnsMany listOf(
      networkError.left(),
      USERS.right(),
    )

    test(
      vmProducer = { vm },
      intents = flowOf(ViewIntent.Retry),
      expectedStates = listOf(
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = networkError,
          submittedQuery = query,
          originalQuery = query,
        ),
        ViewState(
          users = emptyList(),
          isLoading = true, // update isLoading
          error = null, // update error
          submittedQuery = query,
          originalQuery = query,
        ),
        ViewState(
          users = USER_ITEMS, // update users
          isLoading = false, // update isLoading
          error = null,
          submittedQuery = query,
          originalQuery = query,
        ),
      ).mapRight(),
      expectedEvents = emptyList(),
      intentsBeforeCollecting = flow {
        emit(ViewIntent.Search(query))
        timeout()
      },
    ) {
      coVerify(exactly = 2) { searchUsersUseCase(query) }
    }
  }

  @Test
  fun test_withRetryIntentWhenError_returnsUserErrorWithProperLoadingState() {
    val query = "#hoc081098"
    val networkError = UserError.NetworkError
    coEvery { searchUsersUseCase(query) } returns networkError.left()

    test(
      vmProducer = { vm },
      intents = flowOf(ViewIntent.Retry),
      expectedStates = listOf(
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = networkError,
          submittedQuery = query,
          originalQuery = query,
        ),
        ViewState(
          users = emptyList(),
          isLoading = true, // update isLoading
          error = null, // update error
          submittedQuery = query,
          originalQuery = query,
        ),
        ViewState(
          users = emptyList(),
          isLoading = false, // update isLoading
          error = networkError, // update error
          submittedQuery = query,
          originalQuery = query,
        ),
      ).mapRight(),
      expectedEvents = listOf(
        SingleEvent.SearchFailure(networkError),
      ).mapRight(),
      intentsBeforeCollecting = flow {
        emit(ViewIntent.Search(query))
        timeout()
      },
    ) {
      coVerify(exactly = 2) { searchUsersUseCase(query) }
    }
  }

  private companion object {
    private val EXTRAS_TIMEOUT = Duration.milliseconds(100)
    private val TOTAL_TIMEOUT = SEARCH_DEBOUNCE_DURATION + EXTRAS_TIMEOUT
    private val SEMI_TIMEOUT = SEARCH_DEBOUNCE_DURATION / 10

    /**
     * Extra delay to emit search intent
     */
    private suspend inline fun timeout() = delay(TOTAL_TIMEOUT)
  }
}
