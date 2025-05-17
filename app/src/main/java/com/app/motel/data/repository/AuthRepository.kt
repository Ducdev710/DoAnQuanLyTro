package com.app.motel.data.repository


import com.app.motel.data.entity.NguoiDungEntity
import com.app.motel.data.local.TenantDAO
import com.app.motel.data.local.UserDAO
import com.app.motel.data.model.CommonUser
import com.app.motel.data.model.Resource
import com.app.motel.data.model.User
import com.app.motel.data.network.ApiMock
import com.app.motel.security.SecurityHelper
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: ApiMock,
    private val userDAO: UserDAO,
    private val tenantDAO: TenantDAO,
    //private val otpService: OtpService
) {
    // Store temporary user data while waiting for OTP verification
    /*private var pendingRegistration: User? = null

    suspend fun sendRegistrationOtp(user: User): Resource<String> {
        return try {
            // Check if username already exists
            if (userDAO.getByUsername(user.username) != null) {
                return Resource.Error(message = "Tên đăng nhập đã tồn tại")
            }

            // Check if email already exists
            if (user.email != null && userDAO.getUserByEmail(user.email) != null) {
                return Resource.Error(message = "Email đã tồn tại")
            }

            // Check if phone already exists
            if (user.phoneNumber != null && userDAO.getUserByPhone(user.phoneNumber) != null) {
                return Resource.Error(message = "Số điện thoại đã tồn tại")
            }

            // Store pending registration
            pendingRegistration = user

            // Generate and send OTP
            val otp = otpService.generateOtp(user.phoneNumber!!)
            val otpSent = otpService.sendOtpSms(user.phoneNumber, otp)

            if (otpSent) {
                Resource.Success("Mã OTP đã được gửi đến số điện thoại của bạn")
            } else {
                Resource.Error(message = "Không thể gửi mã OTP. Vui lòng thử lại sau.")
            }
        } catch (e: Exception) {
            Resource.Error(message = e.toString())
        }
    }

    suspend fun verifyOtpAndRegister(phoneNumber: String, otp: String): Resource<CommonUser> {
        return try {
            val user = pendingRegistration
                ?: return Resource.Error(message = "Không tìm thấy thông tin đăng ký. Vui lòng thử lại.")

            val isOtpValid = otpService.verifyOtp(phoneNumber, otp)

            if (isOtpValid) {
                // OTP is valid, proceed with registration
                val hashedPassword = SecurityHelper.hashPassword(user.password)
                val userWithHashedPassword = user.copy(password = hashedPassword)
                val userEntity = userWithHashedPassword.toEntityRegister()

                userDAO.insertUser(userEntity)
                otpService.clearOtpData(phoneNumber)
                pendingRegistration = null

                // Return user with original password, NOT the hashed one
                Resource.Success(CommonUser.AdminUser(userEntity.toModel().copy(password = user.password)))
            } else {
                Resource.Error(message = "Mã OTP không hợp lệ hoặc đã hết hạn")
            }
        } catch (e: Exception) {
            Resource.Error(message = e.toString())
        }
    }*/

    // Kept for backward compatibility
    suspend fun register(user: User): Resource<CommonUser> {
        // Validate phone number format (10 digits starting with 0)
        if (user.phoneNumber != null && (!user.phoneNumber.startsWith("0") || user.phoneNumber.length != 10 || !user.phoneNumber.all { it.isDigit() })) {
            return Resource.Error(message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0")
        }

        // Validate password length (minimum 6 characters)
        if (user.password.length < 6) {
            return Resource.Error(message = "Mật khẩu phải có ít nhất 6 ký tự")
        }

        // Mã hóa mật khẩu trước khi lưu vào DB
        val hashedPassword = SecurityHelper.hashPassword(user.password)
        val userWithHashedPassword = user.copy(password = hashedPassword)
        val userEntity = userWithHashedPassword.toEntityRegister()

        return try {
            if (userDAO.getByUsername(userEntity.tenDangNhap) != null) {
                Resource.Error(message = "Tên đăng nhập đã tồn tại")
            } else if(userEntity.email == null || userDAO.getUserByEmail(userEntity.email) != null) {
                Resource.Error(message = "Email đã tồn tại")
            } else if(userEntity.soDienThoai != null && userDAO.getUserByPhone(userEntity.soDienThoai) != null) {
                Resource.Error(message = "Số điện thoại đã tồn tại")
            } else {
                userDAO.insertUser(userEntity)
                // Trả về người dùng với mật khẩu gốc, KHÔNG trả về mật khẩu đã băm
                Resource.Success(CommonUser.AdminUser(userEntity.toModel().copy(password = user.password)))
            }
        } catch (e: Exception) {
            Resource.Error(message = e.toString())
        }
    }

    suspend fun login(username: String, password: String): Resource<CommonUser> {
        return try {
            val admin = userDAO.getByUsername(username)
            val user = tenantDAO.getUserByUsername(username)

            when {
                // No account found
                admin == null && user == null -> {
                    Resource.Error(message = "Tài khoản không tồn tại")
                }
                // Admin account exists - use BCrypt verification
                admin != null -> {
                    if (admin.trangThai == NguoiDungEntity.STATE_INACTIVE) {
                        Resource.Error(message = "Tài khoản đang bị khóa")
                    } else if (!SecurityHelper.checkPassword(password, admin.matKhau)) {
                        Resource.Error(message = "Sai mật khẩu")
                    } else {
                        Resource.Success(CommonUser.AdminUser(admin.toModel().copy(password = password)))
                    }
                }
                // Tenant account exists - direct comparison for plaintext passwords
                user != null -> {
                    if (user.biKhoa) {
                        Resource.Error(message = "Tài khoản khách thuê đã bị khóa")
                    } else if (password != user.matKhau) {
                        Resource.Error(message = "Sai mật khẩu")
                    } else {
                        Resource.Success(CommonUser.NormalUser(user.toModel().copy(password = password)))
                    }
                }
                else -> Resource.Error(message = "Lỗi không xác định")
            }
        } catch (e: Exception) {
            Resource.Error(message = e.toString())
        }
    }
}