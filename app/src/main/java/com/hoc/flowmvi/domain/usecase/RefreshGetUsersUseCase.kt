package com.hoc.flowmvi.domain.usecase

import com.hoc.flowmvi.domain.repository.UserRepository
import javax.inject.Inject

class RefreshGetUsersUseCase @Inject constructor(private val userRepository: UserRepository) {
  suspend operator fun invoke() = userRepository.refresh()
}