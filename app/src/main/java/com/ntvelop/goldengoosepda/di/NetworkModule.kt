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
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            android.util.Log.d("API_HTTP_LOG", message)
        }.apply {
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
        val builder = OkHttpClient.Builder()
            .addInterceptor(hostSelectionInterceptor) // Add first to rewrite host
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)

        try {
            // Trust all certificates for local self-signed HTTPS connections
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            
            val sslSocketFactory = sslContext.socketFactory
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        settingsManager: SettingsManager
    ): Retrofit {
        val serverIp = settingsManager.getServerIp()
        
        var cleanInput = serverIp.trim()
        val isHttps = cleanInput.contains("https", ignoreCase = true)
        
        cleanInput = cleanInput.replace("https://", "", ignoreCase = true)
        cleanInput = cleanInput.replace("http://", "", ignoreCase = true)
        
        if (cleanInput.contains("/")) {
            cleanInput = cleanInput.substringBefore("/")
        }
        
        var host = cleanInput
        var port = if (isHttps) 443 else 8000
        
        if (cleanInput.contains(":")) {
            val parts = cleanInput.split(":")
            host = parts[0]
            parts.getOrNull(1)?.toIntOrNull()?.let {
                port = it
            }
        }
        
        val scheme = if (isHttps) "https" else "http"
        val baseUrl = "$scheme://$host:$port/"
        
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
