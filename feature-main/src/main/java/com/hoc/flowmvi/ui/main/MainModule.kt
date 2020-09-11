package com.hoc.flowmvi.ui.main

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@ExperimentalCoroutinesApi
@FlowPreview
val mainModule = module {
  viewModel {
    MainVM(
      getUsersUseCase = get(),
      refreshGetUsers = get(),
      removeUser = get()
    )
  }
}
