package com.hoc.flowmvi.domain.usecase

import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.repository.UserRepository

class SearchUsersUseCase(private val userRepository: UserRepository) {
  suspend operator fun invoke(query: String): List<User> = userRepository.search(query)
}
