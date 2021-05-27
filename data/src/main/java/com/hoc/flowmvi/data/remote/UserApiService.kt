package com.hoc.flowmvi.data.remote

import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

internal interface UserApiService {
  @GET("users")
  suspend fun getUsers(): List<UserResponse>

  @DELETE("users/{id}")
  suspend fun remove(@Path("id") userId: String): UserResponse

  @Headers("Content-Type: application/json")
  @POST("users")
  suspend fun add(@Body user: UserBody): UserResponse

  @GET("users/search")
  suspend fun search(@Query("q") query: String): List<UserResponse>

  companion object {
    operator fun invoke(retrofit: Retrofit) = retrofit.create<UserApiService>()
  }
}
