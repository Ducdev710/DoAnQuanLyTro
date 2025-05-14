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
) {

    suspend fun register(user: User): Resource<CommonUser> {
        // Mã hóa mật khẩu trước khi lưu vào DB
        val hashedPassword = SecurityHelper.hashPassword(user.password)
        val userWithHashedPassword = user.copy(password = hashedPassword)
        val userEntity = userWithHashedPassword.toEntityRegister()

        return try {
            if (userDAO.getByUsername(userEntity.tenDangNhap) != null) {
                Resource.Error(message = "Tên đăng nhập đã tồn tại")
            } else if(userEntity.email == null || userDAO.getUserByEmail(userEntity.email) != null){
                Resource.Error(message = "Email đã tồn tại")
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

            if (admin == null && user == null) {
                Resource.Error(message = "Tài khoản không tồn tại")
            } else if (
                (admin != null && !SecurityHelper.checkPassword(password, admin.matKhau)) &&
                (user != null && !SecurityHelper.checkPassword(password, user.matKhau))
            ) {
                Resource.Error(message = "Sai mật khẩu")
            } else if (admin?.trangThai == NguoiDungEntity.STATE_INACTIVE) {
                Resource.Error(message = "Tài khoản đang bị khóa")
            } else if (user != null && user.biKhoa) {
                Resource.Error(message = "Tài khoản khách thuê đã bị khóa")
            } else {
                Resource.Success(
                    if (admin != null) CommonUser.AdminUser(admin.toModel().copy(password = password))
                    else if (user != null) CommonUser.NormalUser(user.toModel().copy(password = password))
                    else null
                )
            }
        } catch (e: Exception) {
            Resource.Error(message = e.toString())
        }
    }
}