package com.hoc.flowmvi.domain.usecase

import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.repository.UserRepository
import javax.inject.Inject

class RemoveUserUseCase @Inject constructor(private val userRepository: UserRepository) {
  suspend operator fun invoke(user: User) = userRepository.remove(user)
}