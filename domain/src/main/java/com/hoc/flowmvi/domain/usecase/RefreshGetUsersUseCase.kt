package com.hoc.flowmvi.domain.usecase

import arrow.core.Either
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.repository.UserRepository

class RefreshGetUsersUseCase(private val userRepository: UserRepository) {
  suspend operator fun invoke(): Either<UserError, Unit> = userRepository.refresh()
}
