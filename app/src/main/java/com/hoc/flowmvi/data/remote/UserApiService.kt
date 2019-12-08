package com.hoc.flowmvi.data.remote

import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApiService {
  @GET("users")
  suspend fun getUsers(): List<UserResponse>

  @DELETE("users/{id}")
  suspend fun remove(@Path("id") userId: String): UserResponse

  companion object {
    operator fun invoke(retrofit: Retrofit) = retrofit.create<UserApiService>()
  }
}