package com.ntvelop.goldengoosepda.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HostSelectionInterceptor @Inject constructor(
    private val settingsManager: SettingsManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val currentIp = settingsManager.getServerIp()
        
        val newUrl = request.url.newBuilder()
            .host(currentIp)
            .build()
            
        request = request.newBuilder()
            .url(newUrl)
            .build()
            
        return chain.proceed(request)
    }
}
