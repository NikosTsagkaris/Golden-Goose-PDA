package com.ntvelop.goldengoosepda.di

import com.ntvelop.goldengoosepda.network.AuthInterceptor
import com.ntvelop.goldengoosepda.network.GoldenGooseApiService
import com.ntvelop.goldengoosepda.network.HostSelectionInterceptor
import com.ntvelop.goldengoosepda.network.SettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        hostSelectionInterceptor: HostSelectionInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(hostSelectionInterceptor) // Add first to rewrite host
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        settingsManager: SettingsManager
    ): Retrofit {
        val baseUrl = "http://${settingsManager.getServerIp()}:8000/"
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): GoldenGooseApiService {
        return retrofit.create(GoldenGooseApiService::class.java)
    }
}
