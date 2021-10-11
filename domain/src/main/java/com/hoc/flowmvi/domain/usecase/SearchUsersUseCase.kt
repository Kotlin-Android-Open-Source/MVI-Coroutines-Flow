package com.hoc.flowmvi.domain.usecase

import arrow.core.Either
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.repository.UserError
import com.hoc.flowmvi.domain.repository.UserRepository

class SearchUsersUseCase(private val userRepository: UserRepository) {
  suspend operator fun invoke(query: String): Either<UserError, List<User>> =
    userRepository.search(query)
}
