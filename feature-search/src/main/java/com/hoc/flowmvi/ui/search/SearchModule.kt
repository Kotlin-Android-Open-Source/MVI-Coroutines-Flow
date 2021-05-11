package com.hoc.flowmvi.ui.search

import com.hoc.flowmvi.core.navigator.IntentProviders
import org.koin.dsl.module

val searchModule = module {
  single<IntentProviders.Search> { SearchActivity.IntentProvider() }
}
