package com.hoc.flowmvi.domain.usecase

import arrow.core.Either
import com.hoc.flowmvi.domain.model.User
import com.hoc.flowmvi.domain.model.UserError
import com.hoc.flowmvi.domain.repository.UserRepository

class RemoveUserUseCase(private val userRepository: UserRepository) {
  suspend operator fun invoke(user: User): Either<UserError, Unit> = userRepository.remove(user)
}
