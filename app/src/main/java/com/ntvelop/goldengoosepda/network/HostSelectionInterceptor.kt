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
        
        var cleanInput = currentIp.trim()
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
        
        val newUrl = request.url.newBuilder()
            .scheme(if (isHttps) "https" else "http")
            .host(host)
            .port(port)
            .build()
            
        request = request.newBuilder()
            .url(newUrl)
            .build()
            
        return chain.proceed(request)
    }
}
