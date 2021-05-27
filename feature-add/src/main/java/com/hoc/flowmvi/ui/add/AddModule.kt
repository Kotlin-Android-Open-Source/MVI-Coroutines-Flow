package com.hoc.flowmvi.ui.add

import com.hoc.flowmvi.core.navigator.IntentProviders
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@ExperimentalCoroutinesApi
val addModule = module {
  viewModel { params ->
    AddVM(
      addUser = get(),
      savedStateHandle = params.get(),
    )
  }

  single<IntentProviders.Add> { AddActivity.IntentProvider() }
}
