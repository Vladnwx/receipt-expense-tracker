package com.qrcode.scanner.domain.fns

interface FnsAuthService {

    suspend fun requestCode(phone: String): AuthCodeResult

    suspend fun confirmCode(sessionId: String, code: String): AuthSession

    suspend fun login(login: String, password: String): AuthSession

    suspend fun refreshSession(sessionId: String): AuthSession

    suspend fun logout(sessionId: String)

    suspend fun getActiveSession(): AuthSession?

    suspend fun isAuthenticated(): Boolean

    data class AuthCodeResult(
        val sessionId: String,
        val phoneMask: String,
        val expiresIn: Long
    )

    data class AuthSession(
        val sessionId: String,
        val cookies: String,
        val deviceId: String?,
        val phone: String?,
        val createdAt: Long,
        val expiresAt: Long?
    )

    sealed class AuthError {
        data object InvalidCredentials : AuthError()
        data object ExpiredCode : AuthError()
        data object NetworkError : AuthError()
        data class ServiceError(val message: String) : AuthError()
    }
}
