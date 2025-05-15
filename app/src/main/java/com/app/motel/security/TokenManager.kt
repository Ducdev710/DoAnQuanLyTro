/*
package com.app.motel.security

import com.app.motel.data.entity.VerificationTokenEntity
import com.app.motel.data.local.VerificationTokenDAO
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class TokenManager @Inject constructor(
    private val tokenDAO: VerificationTokenDAO
) {
    fun generateVerificationToken(userId: String): String {
        // Xóa các token cũ chưa sử dụng của user này
        tokenDAO.deleteUnusedTokensByUserId(userId)

        // Xóa các token đã hết hạn
        tokenDAO.deleteExpiredTokens(System.currentTimeMillis())

        val token = generateRandomToken()
        val expiryDate = Calendar.getInstance().apply {
            add(Calendar.HOUR, 24) // Token hết hạn sau 24 giờ
        }.timeInMillis

        val tokenEntity = VerificationTokenEntity(
            token = token,
            userId = userId,
            expiryDate = expiryDate,
            isUsed = false
        )

        tokenDAO.insertToken(tokenEntity)
        return token
    }

    fun verifyToken(token: String): String? {
        val tokenEntity = tokenDAO.getByToken(token) ?: return null

        // Kiểm tra token có hợp lệ không
        if (tokenEntity.isUsed || tokenEntity.expiryDate < System.currentTimeMillis()) {
            return null
        }

        // Đánh dấu token đã được sử dụng
        tokenDAO.markTokenAsUsed(token)
        return tokenEntity.userId
    }

    private fun generateRandomToken(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..32)
            .map { allowedChars[Random.nextInt(0, allowedChars.size)] }
            .joinToString("")
    }
}*/
