package com.hoc.flowmvi.koin

import com.hoc.flowmvi.data.UserResponseToUserDomainMapper
import com.hoc.flowmvi.data.remote.UserApiService
import com.hoc.flowmvi.data.remote.UserResponse
import com.hoc.flowmvi.domain.Mapper
import com.hoc.flowmvi.domain.User
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val BASE_URL = "BASE_URL"

val dataModule = module {
  single { UserApiService(get()) }

  single { UserResponseToUserDomainMapper() as Mapper<UserResponse, User> }

  single { provideRetrofit(get(named(BASE_URL))) }

  single(named(BASE_URL)) { "https://hoc081098.github.io/hoc081098.github.io/users.json" }
}

private fun provideRetrofit(baseUrl: String): Retrofit {
  return Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create())
    .baseUrl(baseUrl)
    .build()
}