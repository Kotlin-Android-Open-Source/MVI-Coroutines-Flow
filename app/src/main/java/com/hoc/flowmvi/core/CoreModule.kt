package com.hoc.flowmvi.core

import com.hoc.flowmvi.core.dispatchers.AppCoroutineDispatchers
import com.hoc.flowmvi.core_ui.navigator.Navigator
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@JvmField
val coreModule = module {
  singleOf(::DefaultAppCoroutineDispatchers) { bind<AppCoroutineDispatchers>() }

  singleOf(::NavigatorImpl) { bind<Navigator>() }
}
