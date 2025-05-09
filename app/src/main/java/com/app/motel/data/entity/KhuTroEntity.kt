package com.app.motel.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.app.motel.data.model.BoardingHouse

@Entity(
    tableName = "KhuTro",
    foreignKeys = [
        ForeignKey(
            entity = NguoiDungEntity::class,
            parentColumns = ["ID"],
            childColumns = ["MaChuNha"],
            onDelete = ForeignKey.SET_NULL,
        )
    ]
)
data class KhuTroEntity(
    @PrimaryKey
    @ColumnInfo(name = "ID")
    val id: String,

    @ColumnInfo(name = "TenKhuTro")
    val tenKhuTro: String,

    @ColumnInfo(name = "DiaChi")
    val diaChi: String,

    @ColumnInfo(name = "SoLuongPhong")
    val soLuongPhong: Int?,

    @ColumnInfo(name = "MaChuNha")
    val maChuNha: String? = null,

    @ColumnInfo(name = "TrangThai")
    val trangThai: Int?,

    @ColumnInfo(name = "MoTa")
    val moTa: String?
){
    companion object{
        const val STATE_ACTIVE = 1
        const val STATE_INACTIVE = 0
    }
    fun toModel(): BoardingHouse {
        return BoardingHouse(
            id = id,
            name = tenKhuTro,
            address = diaChi,
            roomCount = soLuongPhong,
            ownerId = maChuNha,
            isActive = trangThai == 1,
            description = moTa
        )
    }
}
