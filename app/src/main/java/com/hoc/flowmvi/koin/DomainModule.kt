package com.hoc.flowmvi.koin

import com.hoc.flowmvi.core.dispatchers.CoroutineDispatchers
import com.hoc.flowmvi.core.dispatchers.CoroutineDispatchersImpl
import com.hoc.flowmvi.data.UserRepositoryImpl
import com.hoc.flowmvi.data.mapper.UserDomainToUserBodyMapper
import com.hoc.flowmvi.data.mapper.UserDomainToUserResponseMapper
import com.hoc.flowmvi.data.mapper.UserResponseToUserDomainMapper
import com.hoc.flowmvi.domain.repository.UserRepository
import com.hoc.flowmvi.domain.usecase.AddUserUseCase
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.dsl.module

@ExperimentalCoroutinesApi
@FlowPreview
val domainModule = module {
  single<CoroutineDispatchers> { CoroutineDispatchersImpl() }

  single<UserRepository> {
    UserRepositoryImpl(
      userApiService = get(),
      dispatchers = get(),
      responseToDomain = get<UserResponseToUserDomainMapper>(),
      domainToResponse = get<UserDomainToUserResponseMapper>(),
      domainToBody = get<UserDomainToUserBodyMapper>()
    )
  }

  factory { GetUsersUseCase(userRepository = get()) }

  factory { RefreshGetUsersUseCase(userRepository = get()) }

  factory { RemoveUserUseCase(userRepository = get()) }

  factory { AddUserUseCase(userRepository = get()) }
}
