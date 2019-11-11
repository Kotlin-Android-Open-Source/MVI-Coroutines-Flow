package com.hoc.flowmvi.domain.usecase

import com.hoc.flowmvi.domain.repository.UserRepository

class GetUsersUseCase(private val userRepository: UserRepository) {
  suspend operator fun invoke() = userRepository.getUsers()
}