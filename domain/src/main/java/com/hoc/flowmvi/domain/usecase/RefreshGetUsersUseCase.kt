package com.hoc.flowmvi.domain.usecase

import com.hoc.flowmvi.domain.repository.UserRepository

class RefreshGetUsersUseCase(private val userRepository: UserRepository) {
  suspend operator fun invoke() = userRepository.refresh()
}
