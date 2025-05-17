/*
package com.app.motel.common.service

import kotlinx.coroutines.delay
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtpService @Inject constructor() {

    private val otpMap = mutableMapOf<String, OtpData>()
    private val otpLength = 6
    private val otpValidityMillis = 3 * 60 * 1000 // 3 minutes

    data class OtpData(
        val code: String,
        val generatedAt: Long,
        var verified: Boolean = false
    )

    fun generateOtp(phoneNumber: String): String {
        val random = Random()
        val otp = StringBuilder()

        repeat(otpLength) {
            otp.append(random.nextInt(10))
        }

        val otpCode = otp.toString()
        otpMap[phoneNumber] = OtpData(
            code = otpCode,
            generatedAt = System.currentTimeMillis()
        )

        // Add more prominent logging for emulator testing
        android.util.Log.e("OTP_DEBUG", "============================================")
        android.util.Log.e("OTP_DEBUG", "Generated OTP for $phoneNumber: $otpCode")
        android.util.Log.e("OTP_DEBUG", "============================================")

        return otpCode
    }

    fun verifyOtp(phoneNumber: String, inputOtp: String): Boolean {
        val otpData = otpMap[phoneNumber]

        // Debug logging
        android.util.Log.e("OTP_DEBUG", "Verifying OTP: $inputOtp for phone: $phoneNumber")
        android.util.Log.e("OTP_DEBUG", "Stored OTPs: ${debugStoredOtps()}")

        if (otpData == null) {
            android.util.Log.e("OTP_DEBUG", "No OTP entry found for $phoneNumber")
            return false
        }

        // Check if OTP is expired
        if (System.currentTimeMillis() - otpData.generatedAt > otpValidityMillis) {
            android.util.Log.e("OTP_DEBUG", "OTP expired for $phoneNumber")
            otpMap.remove(phoneNumber)
            return false
        }

        // Check if OTP matches
        val isValid = otpData.code == inputOtp
        android.util.Log.e("OTP_DEBUG", "OTP validation result: $isValid (Expected: ${otpData.code}, Got: $inputOtp)")

        // If valid, mark as verified
        if (isValid) {
            otpData.verified = true
        }

        return isValid
    }

    fun isOtpVerified(phoneNumber: String): Boolean {
        return otpMap[phoneNumber]?.verified == true
    }

    fun clearOtpData(phoneNumber: String) {
        otpMap.remove(phoneNumber)
    }

    // In a real application, this would send an SMS
    // This is a mock implementation for testing
    suspend fun sendOtpSms(phoneNumber: String, otpCode: String): Boolean {
        // Simulate network delay
        delay(1000)

        // Log for testing purposes (remove in production)
        android.util.Log.d("OtpService", "Sending OTP $otpCode to $phoneNumber")

        // In a real app, you would use an SMS gateway API here
        // For now, we'll just return true to simulate successful sending
        return true
    }

    // Debug method to see all stored OTPs - useful for troubleshooting
    private fun debugStoredOtps(): String {
        return otpMap.entries.joinToString("\n") { (phone, data) ->
            "$phone -> OTP: ${data.code}, Generated: ${data.generatedAt}, Verified: ${data.verified}"
        }
    }

    // Helper method to get stored OTP for a specific phone number (for debugging)
    fun getStoredOtpForDebug(phoneNumber: String): String? {
        return otpMap[phoneNumber]?.code
    }

    // Method to normalize phone numbers (if that's causing the issue)
    fun normalizePhoneNumber(phoneNumber: String): String {
        // Remove spaces, dashes, parentheses
        var normalized = phoneNumber.replace(Regex("[ \\-()]"), "")

        // Ensure starts with country code if not present
        if (!normalized.startsWith("+")) {
            if (normalized.startsWith("0")) {
                normalized = "+84" + normalized.substring(1)
            }
        }

        return normalized
    }
}*/
