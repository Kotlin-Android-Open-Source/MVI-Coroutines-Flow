package com.hoc.flowmvi.domain.usecase

import arrow.core.Either
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.repository.UserError
import com.hoc.flowmvi.domain.repository.UserRepository

class AddUserUseCase(private val userRepository: UserRepository) {
                suspend operator fun invoke(user: User): Either<UserError, Unit> = userRepository.add(user)
}
