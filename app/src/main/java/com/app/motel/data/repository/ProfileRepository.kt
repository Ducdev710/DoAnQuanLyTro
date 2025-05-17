package com.app.motel.data.repository

import android.content.SharedPreferences
import com.app.motel.common.AppConstants
import com.app.motel.data.local.TenantDAO
import com.app.motel.data.local.UserDAO
import com.app.motel.data.model.CommonUser
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Tenant
import com.app.motel.data.model.User
import com.app.motel.security.SecurityHelper
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val prefs: SharedPreferences,
    private val userDAO: UserDAO,
    private val tenantDAO: TenantDAO,
) {
    fun saveCurrentUser(user: CommonUser){
        prefs.edit().putString(AppConstants.USER_ID_KEY, user.id).apply()
    }

    fun removeCurrentUser(){
        prefs.edit().remove(AppConstants.USER_ID_KEY).apply()
    }

    fun getCurrentUserId(): String {
        return prefs.getString(AppConstants.USER_ID_KEY, "") ?: ""
    }

    suspend fun getCurrentUser(): CommonUser? {
        val userId = prefs.getString(AppConstants.USER_ID_KEY, null)
        if(userId.isNullOrBlank()) return null

        val admin = userDAO.getById(userId)?.toModel()
        val user = tenantDAO.getById(userId)?.toModel()

        if(admin != null) return CommonUser.AdminUser(admin)
        else if(user != null) return CommonUser.NormalUser(user)
        return  null
    }

    suspend fun updateCurrentUser(user: CommonUser): Resource<CommonUser> {
        try {
            if(user.isAdmin){
                val adminUser = user.child as User

                // Validate phone number if it exists
                if (adminUser.phoneNumber != null && (!adminUser.phoneNumber.startsWith("0") ||
                            adminUser.phoneNumber.length != 10 || !adminUser.phoneNumber.all { it.isDigit() })) {
                    return Resource.Error(message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0")
                }

                // Validate password if it's being changed - KEEP for admin users
                val existingUser = userDAO.getById(adminUser.id)
                if (existingUser != null && existingUser.matKhau != adminUser.password && adminUser.password.length < 6) {
                    return Resource.Error(message = "Mật khẩu phải có ít nhất 6 ký tự")
                }

                val passwordToSave = if (existingUser != null && existingUser.matKhau != adminUser.password) {
                    SecurityHelper.hashPassword(adminUser.password)
                } else {
                    adminUser.password // Keep original password (already hashed)
                }

                val updatedUser = adminUser.copy(password = passwordToSave)
                val userEntity = updatedUser.toEntity()
                userDAO.update(userEntity)

                return Resource.Success(
                    CommonUser.AdminUser(userEntity.toModel().copy(password = adminUser.password)),
                    message = "Cập nhật thành công"
                )
            } else {
                val tenantUser = user.child as Tenant

                // Validate phone number if it exists (same for all users)
                if (tenantUser.phoneNumber != null && (!tenantUser.phoneNumber.startsWith("0") ||
                            tenantUser.phoneNumber.length != 10 || !tenantUser.phoneNumber.all { it.isDigit() })) {
                    return Resource.Error(message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0")
                }

                // REMOVED password validation for tenant users

                val updatedTenant = tenantUser.copy()
                val userEntity = updatedTenant.toEntity()
                tenantDAO.update(userEntity)

                return Resource.Success(
                    CommonUser.NormalUser(userEntity.toModel()),
                    message = "Cập nhật thành công"
                )
            }
        } catch (e: Exception) {
            return Resource.Error(message = e.message ?: "Unknown error")
        }
    }

    // Also update the overloaded method for bank information
    suspend fun updateCurrentUser(user: CommonUser, bankName: String?, accountNumber: String?): Resource<CommonUser> {
        try {
            if (user.isAdmin) {
                val adminUser = user.child as User

                // Validate phone number if it exists
                if (adminUser.phoneNumber != null && (!adminUser.phoneNumber.startsWith("0") ||
                            adminUser.phoneNumber.length != 10 || !adminUser.phoneNumber.all { it.isDigit() })) {
                    return Resource.Error(message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0")
                }

                // Keep password validation for admin users
                val existingUser = userDAO.getById(adminUser.id)
                if (existingUser != null && existingUser.matKhau != adminUser.password && adminUser.password.length < 6) {
                    return Resource.Error(message = "Mật khẩu phải có ít nhất 6 ký tự")
                }

                val passwordToSave = if (existingUser != null && existingUser.matKhau != adminUser.password) {
                    SecurityHelper.hashPassword(adminUser.password)
                } else {
                    adminUser.password // Keep original password (already hashed)
                }

                val updatedUser = adminUser.copy(
                    bankName = bankName,
                    accountNumber = accountNumber,
                    password = passwordToSave
                )

                val userEntity = updatedUser.toEntity()
                userDAO.update(userEntity)

                return Resource.Success(
                    CommonUser.AdminUser(userEntity.toModel().copy(password = adminUser.password)),
                    message = "Cập nhật thành công"
                )
            } else {
                // For tenant users, no password validation
                return updateCurrentUser(user)
            }
        } catch (e: Exception) {
            return Resource.Error(message = e.message ?: "Unknown error")
        }
    }

    suspend fun updateAdminUser(user: User): Resource<CommonUser> {
        try {
            val existingUser = userDAO.getById(user.id)
                ?: return Resource.Error(message = "User not found")

            // Check if password has changed and hash it if necessary
            val passwordToSave = if (existingUser.matKhau != user.password) {
                SecurityHelper.hashPassword(user.password)
            } else {
                user.password // Keep the original (already hashed) password
            }

            val updatedUser = user.copy(password = passwordToSave)

            // Update in database
            val userEntity = updatedUser.toEntity()
            userDAO.update(userEntity)

            // Return success with original non-hashed password for display
            return Resource.Success(
                CommonUser.AdminUser(userEntity.toModel().copy(password = user.password)),
                message = "Cập nhật thành công"
            )
        } catch (e: Exception) {
            return Resource.Error(message = e.message ?: "Unknown error")
        }
    }

    suspend fun updateTenantPassword(userId: String, newPassword: String): Resource<CommonUser> {
        return try {
            val tenant = tenantDAO.getTenantById(userId)
                ?: return Resource.Error(message = "Không tìm thấy tài khoản khách thuê")

            // Update the tenant with the new plaintext password
            val updatedTenant = tenant.copy(matKhau = newPassword)
            tenantDAO.update(updatedTenant)

            // Return success with the updated user
            return Resource.Success(
                CommonUser.NormalUser(updatedTenant.toModel()),
                message = "Cập nhật mật khẩu thành công"
            )
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "Lỗi không xác định")
        }
    }

    suspend fun updateTenantUser(tenant: Tenant): Resource<CommonUser> {
        try {
            val existingTenant = tenantDAO.getTenantById(tenant.id)
                ?: return Resource.Error(message = "Không tìm thấy tài khoản khách thuê")

            // Always use the password as plaintext without hashing
            val updatedTenant = tenant.toEntity()
            tenantDAO.update(updatedTenant)

            return Resource.Success(
                CommonUser.NormalUser(updatedTenant.toModel()),
                message = "Cập nhật thành công"
            )
        } catch (e: Exception) {
            return Resource.Error(message = e.message ?: "Lỗi không xác định")
        }
    }
}