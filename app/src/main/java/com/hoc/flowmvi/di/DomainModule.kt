package com.hoc.flowmvi.di

import com.hoc.flowmvi.data.UserRepositoryImpl
import com.hoc.flowmvi.domain.dispatchers.CoroutineDispatchers
import com.hoc.flowmvi.domain.dispatchers.CoroutineDispatchersImpl
import com.hoc.flowmvi.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Module
@InstallIn(ApplicationComponent::class)
abstract class DomainModule {
  @Binds
  abstract fun bindCoroutineDispatchers(coroutineDispatchersImpl: CoroutineDispatchersImpl): CoroutineDispatchers

  @Binds
  abstract fun bindUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository
}
