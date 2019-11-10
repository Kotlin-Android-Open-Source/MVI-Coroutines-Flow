package com.hoc.flowmvi.koin

import com.hoc.flowmvi.ui.MainVM
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
  viewModel { MainVM(get()) }
}