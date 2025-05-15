/*
package com.app.motel.security

import android.content.Context
import androidx.work.*
import com.app.motel.common.AppConstants
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class EmailService @Inject constructor(
    private val context: Context
) {
    // Hàm gửi email xác thực đăng ký
    suspend fun sendVerificationEmail(
        recipientEmail: String,
        verificationToken: String,
        userName: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Tạo WorkRequest để gửi email trong background
                val data = Data.Builder()
                    .putString(KEY_RECIPIENT_EMAIL, recipientEmail)
                    .putString(KEY_TOKEN, verificationToken)
                    .putString(KEY_USER_NAME, userName)
                    .build()

                val emailWorkRequest = OneTimeWorkRequestBuilder<EmailSendWorker>()
                    .setInputData(data)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()

                WorkManager.getInstance(context).enqueue(emailWorkRequest)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    companion object {
        const val KEY_RECIPIENT_EMAIL = "recipient_email"
        const val KEY_TOKEN = "verification_token"
        const val KEY_USER_NAME = "user_name"
    }
}*/
