package com.hoc.flowmvi.koin

import com.hoc.flowmvi.data.UserRepositoryImpl
import com.hoc.flowmvi.domain.CoroutineDispatchers
import com.hoc.flowmvi.domain.CoroutineDispatchersImpl
import com.hoc.flowmvi.domain.GetUsersUseCase
import com.hoc.flowmvi.domain.UserRepository
import org.koin.dsl.module

val domainModule = module {
  single { CoroutineDispatchersImpl() as CoroutineDispatchers }

  single { UserRepositoryImpl(get(), get(), get()) as UserRepository }

  single { GetUsersUseCase(get()) }
}