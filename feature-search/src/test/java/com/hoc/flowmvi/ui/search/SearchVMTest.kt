package com.hoc.flowmvi.ui.search

import androidx.lifecycle.SavedStateHandle
import arrow.core.left
import arrow.core.right
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.usecase.SearchUsersUseCase
import com.hoc.flowmvi.mvi_testing.BaseMviViewModelTest
import com.hoc.flowmvi.mvi_testing.mapRight
import com.hoc.flowmvi.mvi_testing.returnsManyWithDelay
import com.hoc.flowmvi.mvi_testing.returnsWithDelay
import com.hoc.flowmvi.ui.search.SearchVM.Companion.SEARCH_DEBOUNCE_DURATION
import com.hoc081098.flowext.concatWith
import com.hoc081098.flowext.timer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

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
      savedStateHandle = savedStateHandle,
      stateSaver = ViewState.StateSaver(),
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
    coEvery { searchUsersUseCase(query) } returnsWithDelay USERS.right()

    runVMTest(
      vmProducer = { vm },
      intents = flowOf("a", "b", "c", query)
        .map { ViewIntent.Search(it) }
        .onEach { delay(SEMI_TIMEOUT) }
        .onCompletion { timeout() },
      expectedStates = listOf(
        ViewState.initial(""),
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
    ) {
      coVerify(exactly = 1) { searchUsersUseCase(query) }
    }
  }

  @Test
  fun test_withSearchIntent_debounceSearchQueryAndRejectBlank() {
    val query = "     "

    runVMTest(
      vmProducer = { vm },
      intents = flowOf("a", "b", "c", query)
        .map { ViewIntent.Search(it) }
        .onEach { delay(SEMI_TIMEOUT) }
        .onCompletion { timeout() },
      expectedStates = listOf(
        ViewState.initial(""),
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
    )
  }

  @Test
  fun test_withSearchIntent_debounceSearchQueryThenRejectBlankAndDistinctUntilChanged() {
    val query = "#query"
    coEvery { searchUsersUseCase(query) } returnsWithDelay USERS.right()

    runVMTest(
      vmProducer = { vm },
      intents = flowOf("a", "b", "c", query)
        .map { ViewIntent.Search(it) }
        .onEach { delay(SEMI_TIMEOUT) }
        .onCompletion { timeout() }
        .concatWith(timer(ViewIntent.Search(query), TOTAL_TIMEOUT))
        .onCompletion { timeout() },
      expectedStates = listOf(
        ViewState.initial(""),
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
    ) {
      coVerify(exactly = 1) { searchUsersUseCase(query) }
    }
  }

  @Test
  fun test_withSearchIntent_debounceSearchQueryThenRejectBlankThenDistinctUntilChangedAndCancelledPreviousExecution() {
    val query1 = "#query#1"
    val query2 = "#query#2"
    coEvery { searchUsersUseCase(query1) } coAnswers {
      repeat(10) { timeout() } // (1) very long... -> cancelled by (2)
      USERS.right()
    }
    coEvery { searchUsersUseCase(query2) } returns USERS.right()

    runVMTest(
      vmProducer = { vm },
      intents = flowOf("a", "b", "c", query1)
        .map { ViewIntent.Search(it) }
        .onEach { delay(SEMI_TIMEOUT) }
        .onCompletion { timeout() }
        .concatWith(
          timer(
            ViewIntent.Search(query2),
            TOTAL_TIMEOUT,
          ).onCompletion { timeout() }, // (2)
        ),
      expectedStates = listOf(
        ViewState.initial(""),
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
          originalQuery = query1, // update originalQuery
        ),
        ViewState(
          users = emptyList(),
          isLoading = true, // update isLoading
          error = null,
          submittedQuery = "",
          originalQuery = query1,
        ),
        ViewState(
          users = emptyList(),
          isLoading = true,
          error = null,
          submittedQuery = "",
          originalQuery = query2, // update originalQuery
        ),
        ViewState(
          users = USER_ITEMS, // update users
          isLoading = false, // update isLoading
          error = null,
          submittedQuery = query2, // update submittedQuery
          originalQuery = query2,
        ),
      ).mapRight(),
      expectedEvents = emptyList(),
    ) {
      coVerifySequence {
        searchUsersUseCase(query1)
        searchUsersUseCase(query2)
      }
    }
  }

  @Test
  fun test_withSearchIntent_returnsUserItemsWithProperLoadingState() {
    val query = "query"
    coEvery { searchUsersUseCase(query) } returnsWithDelay USERS.right()

    runVMTest(
      vmProducer = { vm },
      intents = flow {
        emit(ViewIntent.Search(query))
        timeout()
      },
      expectedStates = listOf(
        ViewState.initial(""),
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
    ) {
      coVerify(exactly = 1) { searchUsersUseCase(query) }
    }
  }

  @Test
  fun test_withSearchIntent_returnsUserErrorWithProperLoadingState() {
    val query = "query"
    val networkError = UserError.NetworkError
    coEvery { searchUsersUseCase(query) } returnsWithDelay networkError.left()

    runVMTest(
      vmProducer = { vm },
      intents = flow {
        emit(ViewIntent.Search(query))
        timeout()
      },
      expectedStates = listOf(
        ViewState.initial(""),
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
    ) {
      coVerify(exactly = 1) { searchUsersUseCase(query) }
    }
  }

  @Test
  fun test_withRetryIntentWhenNoError_ignored() {
    runVMTest(
      vmProducer = { vm },
      intents = flowOf(ViewIntent.Retry),
      expectedStates = listOf(
        ViewState.initial(""),
      ).mapRight(),
      expectedEvents = emptyList(),
    )
  }

  @Test
  fun test_withRetryIntentWhenError_returnsUserItemsWithProperLoadingState() {
    val query = "#hoc081098"
    val networkError = UserError.NetworkError
    coEvery { searchUsersUseCase(query) } returnsManyWithDelay listOf(
      networkError.left(),
      USERS.right(),
    )

    runVMTest(
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
      preProcessingIntents = flow {
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
    coEvery { searchUsersUseCase(query) } returnsWithDelay networkError.left()

    runVMTest(
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
      preProcessingIntents = flow {
        emit(ViewIntent.Search(query))
        timeout()
      },
    ) {
      coVerify(exactly = 2) { searchUsersUseCase(query) }
    }
  }

  @Test
  fun test_withRetryIntentWhenError_cancelledBySearchIntent() {
    val query1 = "#hoc081098#1"
    val query2 = "#hoc081098#2"
    val networkError = UserError.NetworkError

    var count = 0
    coEvery { searchUsersUseCase(query1) } coAnswers {
      when (count++) {
        0 -> networkError.left()
        1 -> {
          repeat(3) { timeout() } // (1) very long ... -> cancelled by (2)
          USERS.right()
        }
        else -> error("Should not reach here!")
      }
    }
    coEvery { searchUsersUseCase(query2) } returns USERS.right()

    runVMTest(
      vmProducer = { vm },
      intents = flowOf<ViewIntent>(ViewIntent.Retry).concatWith(
        flow {
          delay(SEMI_TIMEOUT) // (2) very short ...
          emit(ViewIntent.Search(query2))
          timeout()
        }
      ),
      expectedStates = listOf(
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = networkError,
          submittedQuery = query1,
          originalQuery = query1,
        ),
        ViewState(
          users = emptyList(),
          isLoading = true, // update isLoading
          error = null, // update error
          submittedQuery = query1,
          originalQuery = query1,
        ),
        ViewState(
          users = emptyList(),
          isLoading = true,
          error = null,
          submittedQuery = query1,
          originalQuery = query2, // update originalQuery
        ),
        ViewState(
          users = USER_ITEMS, // update users
          isLoading = false, // update isLoading
          error = null,
          submittedQuery = query2, // update submittedQuery
          originalQuery = query2,
        ),
      ).mapRight(),
      expectedEvents = emptyList(),
      preProcessingIntents = flow {
        emit(ViewIntent.Search(query1))
        timeout()
      }
    ) {
      coVerifySequence {
        searchUsersUseCase(query1)
        searchUsersUseCase(query1)
        searchUsersUseCase(query2)
      }
    }
  }

  private companion object {
    private val EXTRAS_TIMEOUT = 100.milliseconds
    private val TOTAL_TIMEOUT = SEARCH_DEBOUNCE_DURATION + EXTRAS_TIMEOUT
    private val SEMI_TIMEOUT = SEARCH_DEBOUNCE_DURATION / 10

    /**
     * Extra delay to emit search intent
     */
    private suspend inline fun timeout() = delay(TOTAL_TIMEOUT)
  }
}
