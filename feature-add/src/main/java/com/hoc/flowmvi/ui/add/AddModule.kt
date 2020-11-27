package com.hoc.flowmvi.ui.add

import androidx.lifecycle.SavedStateHandle
import com.hoc.flowmvi.core.navigator.IntentProviders
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@ExperimentalCoroutinesApi
@FlowPreview
val addModule = module {
  viewModel { (s: SavedStateHandle) -> AddVM(addUser = get(), savedStateHandle = s) }

  single<IntentProviders.Add> { AddActivity.IntentProvider() }
}
