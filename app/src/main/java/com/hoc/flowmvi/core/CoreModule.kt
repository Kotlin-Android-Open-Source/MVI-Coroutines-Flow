package com.hoc.flowmvi.core

import com.hoc.flowmvi.core.dispatchers.CoroutineDispatchers
import com.hoc.flowmvi.core_ui.navigator.Navigator
import org.koin.dsl.module

@JvmField
val coreModule = module {
  single<CoroutineDispatchers> { DefaultCoroutineDispatchers() }

  single<Navigator> { NavigatorImpl(add = get(), search = get()) }
}
