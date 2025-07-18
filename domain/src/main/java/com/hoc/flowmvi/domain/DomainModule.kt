package com.hoc.flowmvi.domain

import com.hoc.flowmvi.domain.usecase.AddUserUseCase
import com.hoc.flowmvi.domain.usecase.ObserveUsersUseCase
import com.hoc.flowmvi.domain.usecase.RefreshGetUsersUseCase
import com.hoc.flowmvi.domain.usecase.RemoveUserUseCase
import com.hoc.flowmvi.domain.usecase.SearchUsersUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

@JvmField
val domainModule =
  module {
    factoryOf(::ObserveUsersUseCase)

    factoryOf(::RefreshGetUsersUseCase)

    factoryOf(::RemoveUserUseCase)

    factoryOf(::AddUserUseCase)

    factoryOf(::SearchUsersUseCase)
  }
