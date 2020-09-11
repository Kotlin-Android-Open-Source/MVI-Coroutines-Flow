package com.hoc.flowmvi.domain.usecase

import com.hoc.flowmvi.domain.repository.UserRepository

class GetUsersUseCase(private val userRepository: UserRepository) {
  operator fun invoke() = userRepository.getUsers()
}
