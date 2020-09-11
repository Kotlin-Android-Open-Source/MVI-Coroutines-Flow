package com.hoc.flowmvi

import com.hoc.flowmvi.core.dispatchers.CoroutineDispatchers
import com.hoc.flowmvi.core.dispatchers.CoroutineDispatchersImpl
import org.koin.dsl.module

val coreModule = module {
  single<CoroutineDispatchers> { CoroutineDispatchersImpl() }
}
