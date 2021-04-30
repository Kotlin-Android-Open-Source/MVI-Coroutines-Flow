package com.hoc.flowmvi.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class BaseVM<V : MviState> : ViewModel() {
  abstract val viewState: StateFlow<V>
}
