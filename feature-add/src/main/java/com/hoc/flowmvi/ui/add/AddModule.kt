package com.hoc.flowmvi.ui.add

import com.hoc.flowmvi.core_ui.navigator.IntentProviders
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@JvmField
@ExperimentalCoroutinesApi
val addModule = module {
  viewModelOf(::AddVM)

  singleOf(AddActivity::IntentProvider) { bind<IntentProviders.Add>() }

  factoryOf(ViewState::StateSaver)
}
