package com.hoc.flowmvi.core

import com.hoc.flowmvi.core.dispatchers.CoroutineDispatchers
import com.hoc.flowmvi.core.navigator.Navigator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.dsl.module

@FlowPreview
@ExperimentalCoroutinesApi
val coreModule = module {
  single<CoroutineDispatchers> { CoroutineDispatchersImpl() }

  single<Navigator> { NavigatorImpl(add = get()) }
}
