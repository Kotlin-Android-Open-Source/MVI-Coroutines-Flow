package com.hoc.flowmvi.di

import com.hoc.flowmvi.BuildConfig
import com.hoc.flowmvi.data.remote.UserApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

@Module
@InstallIn(ApplicationComponent::class)
class DataModule {
  @Provides
  fun provideUserApiService(retrofit: Retrofit): UserApiService = UserApiService(retrofit)

  @Provides
  fun provideMoshi(): Moshi {
    return Moshi
        .Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
  }

  @Provides
  fun provideRetrofit(
      @BaseUrl baseUrl: String,
      moshi: Moshi,
      client: OkHttpClient,
  ): Retrofit {
    return Retrofit.Builder()
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(baseUrl)
        .build()
  }

  @Provides
  fun provideOkHttpClient(): OkHttpClient {
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

  @Provides
  @BaseUrl
  fun provideBaseUrl(): String = "https://mvi-coroutines-flow-server.herokuapp.com/"
}