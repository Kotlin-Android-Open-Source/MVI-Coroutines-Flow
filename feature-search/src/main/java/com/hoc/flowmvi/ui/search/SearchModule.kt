package com.hoc.flowmvi.ui.search

import com.hoc.flowmvi.core.navigator.IntentProviders
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalTime
val searchModule = module {
  single<IntentProviders.Search> { SearchActivity.IntentProvider() }

  viewModel { SearchVM(searchUsersUseCase = get()) }
}
