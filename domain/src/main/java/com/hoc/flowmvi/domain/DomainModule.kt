package com.hoc.flowmvi.domain

import com.hoc.flowmvi.domain.usecase.AddUserUseCase
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import com.hoc.flowmvi.domain.usecase.SearchUsersUseCase
import org.koin.dsl.module

val domainModule = module {
  factory { GetUsersUseCase(userRepository = get()) }

  factory { RefreshGetUsersUseCase(userRepository = get()) }

  factory { RemoveUserUseCase(userRepository = get()) }

  factory { AddUserUseCase(userRepository = get()) }

  factory { SearchUsersUseCase(userRepository = get()) }
}
