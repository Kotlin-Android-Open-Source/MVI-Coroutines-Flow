package com.hoc.flowmvi.domain.usecase

import com.hoc.flowmvi.domain.repository.UserRepository
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(private val userRepository: UserRepository) {
  operator fun invoke() = userRepository.getUsers()
}