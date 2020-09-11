package com.hoc.flowmvi.koin

import com.hoc.flowmvi.coreModule
import com.hoc.flowmvi.data.dataModule
import com.hoc.flowmvi.domain.domainModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
val allModules = listOf(
  coreModule,
  dataModule,
  domainModule,
  viewModelModule
)
