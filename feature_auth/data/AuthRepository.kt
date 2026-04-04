package com.ntvelop.goldengoosepda.feature_auth.data

import com.ntvelop.goldengoosepda.network.GoldenGooseApiService
import com.ntvelop.goldengoosepda.network.LoginRequest
import com.ntvelop.goldengoosepda.network.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: GoldenGooseApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(pin: String): Result<Unit> {
        return try {
            val response = apiService.login(LoginRequest(pin))
            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                tokenManager.saveToken(tokenResponse.accessToken)
                tokenManager.saveRole(tokenResponse.role)
                tokenManager.saveUserId(tokenResponse.userId)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Invalid PIN"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean = tokenManager.getToken() != null
    fun logout() = tokenManager.clear()
}
