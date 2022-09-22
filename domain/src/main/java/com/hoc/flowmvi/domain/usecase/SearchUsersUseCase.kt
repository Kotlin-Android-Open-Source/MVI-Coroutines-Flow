package com.hoc.flowmvi.domain.usecase

import arrow.core.continuations.EffectScope
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.repository.UserRepository

class SearchUsersUseCase(private val userRepository: UserRepository) {
  context(EffectScope<UserError>)
  suspend operator fun invoke(query: String): List<User> =
    userRepository.search(query)
}
