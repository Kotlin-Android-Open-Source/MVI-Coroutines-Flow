package com.hoc.flowmvi.domain.usecase

import arrow.core.continuations.EffectScope
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.repository.UserRepository

class RefreshGetUsersUseCase(private val userRepository: UserRepository) {
  context(EffectScope<UserError>)
  suspend operator fun invoke(): Unit = userRepository.refresh()
}
