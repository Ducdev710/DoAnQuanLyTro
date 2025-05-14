package com.app.motel.security

import at.favre.lib.crypto.bcrypt.BCrypt

/**
 * Helper class để xử lý các vấn đề bảo mật như mã hóa mật khẩu
 */
class SecurityHelper {
    companion object {
        /**
         * Băm mật khẩu sử dụng BCrypt
         * @param password Mật khẩu dạng plaintext cần băm
         * @return Chuỗi mật khẩu đã được băm
         */
        fun hashPassword(password: String): String {
            return BCrypt.withDefaults().hashToString(12, password.toCharArray())
        }

        /**
         * Kiểm tra mật khẩu plaintext có khớp với mật khẩu đã băm không
         * @param plainPassword Mật khẩu người dùng nhập vào
         * @param hashedPassword Mật khẩu đã băm lưu trong DB
         * @return true nếu mật khẩu khớp, ngược lại là false
         */
        fun checkPassword(plainPassword: String, hashedPassword: String): Boolean {
            val result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword)
            return result.verified
        }
    }
}