package com.app.motel.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.motel.data.model.User

@Entity(tableName = "NguoiDung")
data class NguoiDungEntity(
    @PrimaryKey
    @ColumnInfo(name = "ID")
    val id: String,

    @ColumnInfo(name = "HoTen")
    val hoTen: String,

    @ColumnInfo(name = "TenDangNhap")
    val tenDangNhap: String,

    @ColumnInfo(name = "MatKhau")
    val matKhau: String,

    @ColumnInfo(name = "Email")
    val email: String? = null,

    @ColumnInfo(name = "NgaySinh")
    val ngaySinh: String? = null,

    @ColumnInfo(name = "SoDienThoai")
    val soDienThoai: String? = null,

    @ColumnInfo(name = "TenNganHang")
    val tenNganHang: String? = null,

    @ColumnInfo(name = "SoTaiKhoan")
    val soTaiKhoan: String? = null,

    @ColumnInfo(name = "TrangThai")
    val trangThai: Int? = STATE_ACTIVE
)
{
    companion object{
        const val STATE_INACTIVE = 0
        const val STATE_ACTIVE = 1
    }

    fun toModel(): User {
        return User(
            id = id,
            username = tenDangNhap,
            password = matKhau,
            fullName = hoTen,
            birthDate = ngaySinh,
            phoneNumber = soDienThoai,
            email = email,
            bankName = tenNganHang,
            accountNumber = soTaiKhoan,
            state = trangThai ?: STATE_ACTIVE,
        )
    }
}