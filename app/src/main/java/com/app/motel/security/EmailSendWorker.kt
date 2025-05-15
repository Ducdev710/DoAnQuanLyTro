/*
package com.app.motel.security

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.motel.security.EmailService.Companion.KEY_RECIPIENT_EMAIL
import com.app.motel.security.EmailService.Companion.KEY_TOKEN
import com.app.motel.security.EmailService.Companion.KEY_USER_NAME
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSendWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    // Define constants here as a temporary workaround
    companion object {
        private const val APP_EMAIL = "motelapp.noreply@gmail.com"
        private const val APP_EMAIL_PASSWORD = "your_app_password_here"
        private const val BASE_URL = "https://motelapp.com"
    }

    override suspend fun doWork(): Result {
        val recipientEmail = inputData.getString(KEY_RECIPIENT_EMAIL) ?: return Result.failure()
        val verificationToken = inputData.getString(KEY_TOKEN) ?: return Result.failure()
        val userName = inputData.getString(KEY_USER_NAME) ?: return Result.failure()

        return try {
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    // Use companion object constants instead of BuildConfig
                    return PasswordAuthentication(
                        APP_EMAIL,
                        APP_EMAIL_PASSWORD
                    )
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(APP_EMAIL))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                subject = "Xác nhận tài khoản - Motel App"

                val verificationLink = "$BASE_URL/verify?token=$verificationToken"

                setContent("""
                    <html>
                        <body>
                            <h2>Xin chào $userName,</h2>
                            <p>Cảm ơn bạn đã đăng ký tài khoản tại Motel App.</p>
                            <p>Vui lòng nhấn vào liên kết sau để xác nhận email của bạn:</p>
                            <a href="$verificationLink">Xác nhận tài khoản</a>
                            <p>Liên kết có hiệu lực trong 24 giờ.</p>
                            <p>Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email này.</p>
                            <p>Trân trọng,<br>Đội ngũ Motel App</p>
                        </body>
                    </html>
                """.trimIndent(), "text/html; charset=utf-8")
            }

            Transport.send(message)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}*/
