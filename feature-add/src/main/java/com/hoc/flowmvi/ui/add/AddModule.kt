package com.hoc.flowmvi.ui.add

import com.hoc.flowmvi.core.navigator.IntentProviders
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@ExperimentalCoroutinesApi
@FlowPreview
val addModule = module {
  viewModel {
    AddVM(
      addUser = get(),
      savedStateHandle = it.get(),
    )
  }

  single<IntentProviders.Add> { AddActivity.IntentProvider() }
}
