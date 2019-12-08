package com.hoc.flowmvi.koin

import com.hoc.flowmvi.data.UserDomainToUserResponseMapper
import com.hoc.flowmvi.data.UserRepositoryImpl
import com.hoc.flowmvi.data.UserResponseToUserDomainMapper
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.Mapper
import com.hoc.flowmvi.domain.dispatchers.CoroutineDispatchers
import com.hoc.flowmvi.domain.dispatchers.CoroutineDispatchersImpl
import com.hoc.flowmvi.domain.entity.User
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.repository.UserRepository
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val domainModule = module {
  single { CoroutineDispatchersImpl() as CoroutineDispatchers }

  single {
    UserRepositoryImpl(
      get(),
      get(),
      responseToDomain = get<UserResponseToUserDomainMapper>(),
      domainToResponse = get<UserDomainToUserResponseMapper>()
    ) as UserRepository
  }

  single { GetUsersUseCase(get()) }

  single { RefreshGetUsersUseCase(get()) }

  single { RemoveUserUseCase(get()) }
}