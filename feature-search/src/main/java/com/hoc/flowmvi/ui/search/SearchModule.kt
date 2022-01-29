package com.hoc.flowmvi.ui.search

import com.hoc.flowmvi.core_ui.navigator.IntentProviders
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import kotlin.time.ExperimentalTime

@JvmField
@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalTime
val searchModule = module {
  single<IntentProviders.Search> { SearchActivity.IntentProvider() }

  viewModel { params ->
    SearchVM(
      searchUsersUseCase = get(),
      savedStateHandle = params.get(),
    )
  }
}
