package com.hoc.flowmvi.domain

class GetUsersUseCase(private val userRepository: UserRepository) {
  suspend operator fun invoke() = userRepository.getUsers()
}