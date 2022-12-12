package com.hoc.flowmvi.ui.search

import com.hoc.flowmvi.core_ui.navigator.IntentProviders
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@JvmField
@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalTime
val searchModule = module {
  singleOf(SearchActivity::IntentProvider) { bind<IntentProviders.Search>() }

  viewModelOf(::SearchVM)

  factoryOf(ViewState::StateSaver)
}
