package com.hoc.flowmvi.domain.usecase

import arrow.core.continuations.EffectScope
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.repository.UserRepository

class AddUserUseCase(private val userRepository: UserRepository) {
  context(EffectScope<UserError>)
  suspend operator fun invoke(user: User): Unit = userRepository.add(user)
}
