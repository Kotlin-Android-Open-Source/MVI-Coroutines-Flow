package com.hoc.flowmvi.ui.search

import androidx.lifecycle.SavedStateHandle
import arrow.core.right
import com.flowmvi.mvi_testing.BaseMviViewModelTest
import com.flowmvi.mvi_testing.mapRight
import com.hoc.flowmvi.domain.usecase.SearchUsersUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
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

    searchUsersUseCase = mockk() {
      coEvery { this@mockk(any()) } returns USERS.right()
    }

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
  fun test_withSearchIntent_rejectBlankSearchQuery() {
    val q = "    "
    test(
      vmProducer = { vm },
      intents = flow {
        emit(ViewIntent.Search(q))
        timeout()
      },
      expectedStates = listOf(
        ViewState.initial(null),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = q, // update originalQuery
        ),
      ).mapRight(),
      expectedEvents = emptyList(),
      delayAfterDispatchingIntents = TIMEOUT,
    )
  }

  @Test
  fun test_withSearchIntent_returnsUserItemsWithProperLoadingState() {
    val q = "query"
    test(
      vmProducer = { vm },
      intents = flow {
        emit(ViewIntent.Search(q))
        timeout()
      },
      expectedStates = listOf(
        ViewState.initial(null),
        ViewState(
          users = emptyList(),
          isLoading = false,
          error = null,
          submittedQuery = "",
          originalQuery = q, // update originalQuery
        ),
        ViewState(
          users = emptyList(),
          isLoading = true, // update isLoading
          error = null,
          submittedQuery = "",
          originalQuery = q,
        ),
        ViewState(
          users = USER_ITEMS, // update users
          isLoading = false, // update isLoading
          error = null,
          submittedQuery = q,
          originalQuery = q,
        ),
      ).mapRight(),
      expectedEvents = emptyList(),
      delayAfterDispatchingIntents = TIMEOUT,
    ) {
      coVerify(exactly = 1) { searchUsersUseCase(q) }
    }
  }

  private companion object {
    private val TIMEOUT = Duration.milliseconds(100)

    /**
     * Extra delay to emit search intent
     */
    private suspend inline fun timeout() =
      delay(SearchVM.SEARCH_DEBOUNCE_DURATION + TIMEOUT)
  }
}
