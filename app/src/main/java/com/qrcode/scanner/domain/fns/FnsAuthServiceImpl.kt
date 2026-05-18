package com.qrcode.scanner.domain.fns

import com.qrcode.scanner.data.remote.FnsApiService
import com.qrcode.scanner.data.remote.FnsAuthCodeRequest
import com.qrcode.scanner.data.remote.FnsAuthConfirmRequest
import com.qrcode.scanner.data.reporter.AppLogger
import com.qrcode.scanner.data.repository.FnsAuthRepository
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FnsAuthServiceImpl @Inject constructor(
    private val api: FnsApiService,
    private val authRepository: FnsAuthRepository
) : FnsAuthService {

    private val pendingPhones = mutableMapOf<String, String>()

    override suspend fun requestCode(phone: String): FnsAuthService.AuthCodeResult {
        AppLogger.i("FnsAuth", "requestCode for $phone")
        try {
            val response = api.requestAuthCode(FnsAuthCodeRequest(phone = phone))
            if (response.code != null && response.code != 0) {
                throw Exception(response.message ?: "Ошибка отправки кода")
            }
            val data = response.data
            if (data == null || data.sessionId == null) {
                throw Exception("Пустой ответ от сервера")
            }
            pendingPhones[data.sessionId] = phone
            return FnsAuthService.AuthCodeResult(
                sessionId = data.sessionId,
                phoneMask = maskPhone(phone),
                expiresIn = data.expiresIn ?: 300L
            )
        } catch (e: FnsAuthService.AuthError) {
            throw e
        } catch (e: Exception) {
            Log.e("FnsAuth", "requestCode failed for $phone", e)
            throw FnsAuthService.AuthError.NetworkError
        }
    }

    override suspend fun confirmCode(sessionId: String, code: String): FnsAuthService.AuthSession {
        AppLogger.i("FnsAuth", "confirmCode sessionId=$sessionId")
        val phone = pendingPhones.remove(sessionId)
            ?: throw FnsAuthService.AuthError.ServiceError("Сессия не найдена, повторите отправку кода")
        try {
            val response = api.confirmAuthCode(
                FnsAuthConfirmRequest(phone = phone, code = code)
            )
            if (response.code != null && response.code != 0) {
                throw Exception(response.message ?: "Ошибка подтверждения кода")
            }
            val data = response.data
            if (data == null || data.sessionId == null) {
                throw Exception("Пустой ответ от сервера")
            }
            val now = System.currentTimeMillis()
            val authSession = FnsAuthService.AuthSession(
                sessionId = data.sessionId,
                cookies = data.cookies ?: "",
                deviceId = data.deviceId,
                phone = phone,
                createdAt = now,
                expiresAt = now + 86_400_000L
            )

            com.qrcode.scanner.data.local.entity.FnsSessionEntity(
                sessionId = authSession.sessionId,
                cookies = authSession.cookies,
                deviceId = authSession.deviceId,
                phone = authSession.phone,
                createdAt = authSession.createdAt,
                expiresAt = authSession.expiresAt
            ).let { entity ->
                authRepository.saveSession(entity)
            }

            return authSession
        } catch (e: FnsAuthService.AuthError) {
            throw e
        } catch (e: Exception) {
            Log.e("FnsAuth", "confirmCode failed", e)
            throw FnsAuthService.AuthError.NetworkError
        }
    }

    override suspend fun login(login: String, password: String): FnsAuthService.AuthSession {
        throw UnsupportedOperationException("Вход по логину/паролю не поддерживается")
    }

    override suspend fun refreshSession(sessionId: String): FnsAuthService.AuthSession {
        val entity = authRepository.getActiveSession()
        if (entity != null && entity.expiresAt != null && System.currentTimeMillis() < entity.expiresAt) {
            return FnsAuthService.AuthSession(
                sessionId = entity.sessionId,
                cookies = entity.cookies,
                deviceId = entity.deviceId,
                phone = entity.phone,
                createdAt = entity.createdAt,
                expiresAt = entity.expiresAt
            )
        }
        authRepository.deactivateAll()
        throw FnsAuthService.AuthError.ServiceError("Сессия истекла, войдите заново")
    }

    override suspend fun logout(sessionId: String) {
        AppLogger.i("FnsAuth", "logout sessionId=$sessionId")
        authRepository.deactivateAll()
    }

    override suspend fun getActiveSession(): FnsAuthService.AuthSession? {
        val entity = authRepository.getActiveSession() ?: return null
        if (entity.expiresAt != null && System.currentTimeMillis() >= entity.expiresAt) {
            authRepository.deactivate(entity.id)
            return null
        }
        return FnsAuthService.AuthSession(
            sessionId = entity.sessionId,
            cookies = entity.cookies,
            deviceId = entity.deviceId,
            phone = entity.phone,
            createdAt = entity.createdAt,
            expiresAt = entity.expiresAt
        )
    }

    override suspend fun isAuthenticated(): Boolean {
        return getActiveSession() != null
    }

    private fun maskPhone(phone: String): String {
        if (phone.length < 10) return phone
        return phone.take(2) + "***" + phone.takeLast(4)
    }
}
