package com.hoc.flowmvi.ui.search

import androidx.lifecycle.ViewModel
import com.hoc.flowmvi.domain.usecase.SearchUsersUseCase
import kotlinx.coroutines.flow.MutableSharedFlow

internal class SearchVM(
  private val searchUsersUseCase: SearchUsersUseCase,
) : ViewModel() {
  private val _intent = MutableSharedFlow<ViewIntent>(extraBufferCapacity = 64)

  suspend fun processIntent(intent: ViewIntent) = _intent.emit(intent)

  init {
  }
}
