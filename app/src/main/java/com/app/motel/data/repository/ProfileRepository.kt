package com.app.motel.data.repository

import android.content.SharedPreferences
import com.app.motel.common.AppConstants
import com.app.motel.data.local.TenantDAO
import com.app.motel.data.local.UserDAO
import com.app.motel.data.model.CommonUser
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Tenant
import com.app.motel.data.model.User
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
                val userEntity = (user.child as User).toEntity()
                userDAO.update(userEntity)
                return Resource.Success(CommonUser.AdminUser(userEntity.toModel()), message = "Cập nhật thành công")
            }else {
                val userEntity = (user.child as Tenant).toEntity()
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

    // Add a new overloaded method to handle bank information separately
    suspend fun updateCurrentUser(user: CommonUser, bankName: String?, accountNumber: String?): Resource<CommonUser> {
        try {
            if (user.isAdmin) {
                // Extract the User from CommonUser and update its bank information
                val adminUser = user.child as User
                val updatedUser = adminUser.copy(
                    bankName = bankName,
                    accountNumber = accountNumber
                )

                // Create a new CommonUser with the updated User
                val updatedCommonUser = CommonUser.AdminUser(updatedUser)

                // Update in database
                val userEntity = updatedUser.toEntity()
                userDAO.update(userEntity)

                return Resource.Success(
                    CommonUser.AdminUser(userEntity.toModel()),
                    message = "Cập nhật thành công"
                )
            } else {
                // For non-admin users, just use the standard method
                return updateCurrentUser(user)
            }
        } catch (e: Exception) {
            return Resource.Error(message = e.message ?: "Unknown error")
        }
    }
}