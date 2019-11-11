package com.hoc.flowmvi.koin

import com.hoc.flowmvi.data.UserRepositoryImpl
import com.hoc.flowmvi.domain.dispatchers.CoroutineDispatchers
import com.hoc.flowmvi.domain.dispatchers.CoroutineDispatchersImpl
import com.hoc.flowmvi.domain.usecase.GetUsersUseCase
import com.hoc.flowmvi.domain.repository.UserRepository
import org.koin.dsl.module

val domainModule = module {
  single { CoroutineDispatchersImpl() as CoroutineDispatchers }

  single { UserRepositoryImpl(get(), get(), get()) as UserRepository }

  single { GetUsersUseCase(get()) }
}