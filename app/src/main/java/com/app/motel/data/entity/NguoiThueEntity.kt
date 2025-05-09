package com.app.motel.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.app.motel.data.model.Tenant

@Entity(
    tableName = "NguoiThue",
    foreignKeys = [ForeignKey(
        entity = PhongEntity::class,
        parentColumns = ["ID"],
        childColumns = ["MaPhong"],
        onDelete = ForeignKey.SET_NULL,
    )]
)
data class NguoiThueEntity(
    @PrimaryKey
    @ColumnInfo(name = "ID")
    val id: String,

    @ColumnInfo(name = "HoTen")
    val hoTen: String,

    @ColumnInfo(name = "CCCD")
    val cccd: String? = null,

    @ColumnInfo(name = "QueQuan")
    val queQuan: String? = null,

    @ColumnInfo(name = "NgaySinh")
    val ngaySinh: String? = null,

    @ColumnInfo(name = "SoDienThoai")
    val soDienThoai: String? = null,

    @ColumnInfo(name = "TrangThai")
    val trangThai: String? = null,

    @ColumnInfo(name = "MaPhong")
    val maPhong: String? = null,

    @ColumnInfo(name = "MaKhuTro")
    val maKhuTro: String? = null,

    @ColumnInfo(name = "MaChuNha")
    val maChuNha: String? = null,

    @ColumnInfo(name = "TenDangNhap")
    val tenDangNhap: String,

    @ColumnInfo(name = "MatKhau")
    val matKhau: String,

    @ColumnInfo(name = "BiKhoa", defaultValue = "0")
    val biKhoa: Boolean = false,

    @ColumnInfo(name = "LaChuHopDong", defaultValue = "0")
    val laChuHopDong: Boolean = false
){
    enum class Status(val value: String) {
        ACTIVE("Đang thuê"),
        INACTIVE("Người thuê mới"),
        TEMPORARY_ABSENT("Đã chuyển đi");

        companion object {
            fun fromValue(value: String) = entries.firstOrNull { it.value == value } ?: ACTIVE
        }
    }

    fun toModel() = Tenant(
        id = id,
        fullName = hoTen,
        idCard = cccd,
        homeTown = queQuan,
        birthDay = ngaySinh,
        phoneNumber = soDienThoai,
        status = trangThai,
        roomId = maPhong,
        boardingHouseId = maKhuTro,
        landlordId = maChuNha,
        username = tenDangNhap,
        password = matKhau,
        isLock = biKhoa,
        isContractHolder = laChuHopDong
    )
}