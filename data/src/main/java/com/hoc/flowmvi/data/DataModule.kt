package com.hoc.flowmvi.data

import com.hoc.flowmvi.data.mapper.UserDomainToUserBodyMapper
import com.hoc.flowmvi.data.mapper.UserErrorMapper
import com.hoc.flowmvi.data.mapper.UserResponseToUserDomainMapper
import com.hoc.flowmvi.data.remote.ErrorResponse
import com.hoc.flowmvi.data.remote.UserApiService
import com.hoc.flowmvi.domain.repository.UserRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

val BASE_URL_QUALIFIER = named("BASE_URL")

@FlowPreview
@ExperimentalStdlibApi
@ExperimentalTime
@ExperimentalCoroutinesApi
val dataModule = module {
  single { UserApiService(retrofit = get()) }

  single {
    provideRetrofit(
      baseUrl = get(BASE_URL_QUALIFIER),
      moshi = get(),
      client = get()
    )
  }

  single { provideMoshi() }

  single { provideOkHttpClient() }

  factory(BASE_URL_QUALIFIER) { "https://mvi-coroutines-flow-server.herokuapp.com/" }

  factory { UserResponseToUserDomainMapper() }

  factory { UserDomainToUserBodyMapper() }

  factory { get<Moshi>().adapter<ErrorResponse>() }

  factory { UserErrorMapper(errorResponseJsonAdapter = get()) }

  single<UserRepository> {
    UserRepositoryImpl(
      userApiService = get(),
      dispatchers = get(),
      responseToDomain = get<UserResponseToUserDomainMapper>(),
      domainToBody = get<UserDomainToUserBodyMapper>(),
      errorMapper = get<UserErrorMapper>(),
    )
  }
}

private fun provideMoshi(): Moshi {
  return Moshi
    .Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
}

private fun provideRetrofit(baseUrl: String, moshi: Moshi, client: OkHttpClient): Retrofit {
  return Retrofit.Builder()
    .client(client)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(baseUrl)
    .build()
}

private fun provideOkHttpClient(): OkHttpClient {
  return OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(10, TimeUnit.SECONDS)
    .addInterceptor(
      HttpLoggingInterceptor()
        .apply { level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE }
    )
    .build()
}
