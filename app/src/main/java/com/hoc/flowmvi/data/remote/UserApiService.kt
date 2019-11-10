package com.hoc.flowmvi.data.remote

import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET

interface UserApiService {
  @GET suspend fun getUsers(): List<UserResponse>

  companion object {
    operator fun invoke(retrofit: Retrofit) = retrofit.create<UserApiService>()
  }
}