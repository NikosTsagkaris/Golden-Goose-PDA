package com.ntvelop.goldengoosepda.feature_auth.data

import android.util.Log
import com.google.gson.Gson
import com.ntvelop.goldengoosepda.network.GoldenGooseApiService
import com.ntvelop.goldengoosepda.network.LoginRequest
import com.ntvelop.goldengoosepda.network.TokenManager
import com.ntvelop.goldengoosepda.network.TokenResponse
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: GoldenGooseApiService,
    private val tokenManager: TokenManager
) {
    private val gson = Gson()

    suspend fun login(pin: String, username: String? = null, password: String? = null): Result<Unit> {
        return try {
            val request = LoginRequest(
                pin = pin,
                username = username,
                password = password
            )

            Log.d("AUTH_DEBUG", "================ AUTH LOGIN REQUEST ================")
            Log.d("AUTH_DEBUG", "Request Params -> username: '${username ?: "N/A"}', pin: '$pin', password: '${password ?: "N/A"}'")

            val response = apiService.login(request)
            val statusCode = response.code()
            Log.d("AUTH_DEBUG", "Raw HTTP Response Code: $statusCode")

            val rawBodyString = if (response.isSuccessful) {
                response.body()?.string()
            } else {
                response.errorBody()?.string()
            }

            Log.d("AUTH_DEBUG", "RAW JSON STRING RETURNED BY SERVER: -> $rawBodyString <-")

            if (response.isSuccessful && !rawBodyString.isNullOrBlank()) {
                var tokenToSave: String? = null
                var roleToSave: String? = null
                var userIdToSave: String? = null

                // Attempt 1: Parse with GSON
                try {
                    val parsed = gson.fromJson(rawBodyString, TokenResponse::class.java)
                    if (parsed != null) {
                        tokenToSave = parsed.effectiveToken
                        roleToSave = parsed.effectiveRole
                        userIdToSave = parsed.effectiveUserId
                        Log.d("AUTH_DEBUG", "GSON Parsed TokenResponse -> token: '$tokenToSave', role: '$roleToSave', userId: '$userIdToSave'")
                    }
                } catch (gsonEx: Exception) {
                    Log.e("AUTH_DEBUG", "GSON parsing exception for raw body: '$rawBodyString'", gsonEx)
                }

                // Attempt 2: Fallback manual JSONObject parsing if GSON didn't extract a valid token
                if (tokenToSave.isNullOrEmpty()) {
                    try {
                        val json = JSONObject(rawBodyString)
                        tokenToSave = json.optString("access_token", "")
                            .ifEmpty { json.optString("token", "") }
                            .ifEmpty { json.optString("jwt", "") }
                            .ifEmpty { json.optString("jwt_token", "") }
                            .takeIf { it.isNotBlank() && it != "null" }

                        roleToSave = json.optString("role", "")
                            .ifEmpty { json.optString("user_role", "") }
                            .takeIf { it.isNotBlank() && it != "null" } ?: "WAITER"

                        userIdToSave = json.optString("user_id", "")
                            .ifEmpty { json.optString("id", "") }
                            .takeIf { it.isNotBlank() && it != "null" }

                        Log.d("AUTH_DEBUG", "JSONObject Fallback Parsed -> token: '$tokenToSave', role: '$roleToSave', userId: '$userIdToSave'")
                    } catch (jsonEx: Exception) {
                        Log.e("AUTH_DEBUG", "JSONObject fallback parsing exception for raw body: '$rawBodyString'", jsonEx)
                    }
                }

                if (tokenToSave.isNullOrEmpty()) {
                    tokenToSave = "SESSION_AUTH_OK"
                    Log.d("AUTH_DEBUG", "HTTP 200 OK received without explicit token. Applying Session Fallback Token: '$tokenToSave' (role: '$roleToSave', userId: '$userIdToSave')")
                }

                val savedSuccessfully = tokenManager.saveToken(tokenToSave)
                if (savedSuccessfully) {
                    tokenManager.saveRole(roleToSave)
                    tokenManager.saveUserId(userIdToSave)
                    Log.d("AUTH_DEBUG", "Login Success! Token ('$tokenToSave'), role ('$roleToSave'), userId ('$userIdToSave') saved successfully.")
                    Result.success(Unit)
                } else {
                    Log.e("AUTH_DEBUG", "tokenManager.saveToken returned false for token: '$tokenToSave'")
                    Result.failure(Exception("Login Failed: Token storage error"))
                }
            } else {
                val msg = if (statusCode == 401) "Invalid PIN" else "Login Failed (HTTP $statusCode)"
                Log.e("AUTH_DEBUG", "Login HTTP Request Failed: Code $statusCode, Raw Error Body: '$rawBodyString'")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e("AUTH_DEBUG", "Unhandled exception during login process: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean = tokenManager.getToken() != null
    fun logout() = tokenManager.clear()
}
