package com.app.motel.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.app.motel.data.model.Contract

@Entity(
    tableName = "HopDong",
    foreignKeys = [
        ForeignKey(
            entity = NguoiThueEntity::class,
            parentColumns = ["ID"],
            childColumns = ["MaKhach"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = PhongEntity::class,
            parentColumns = ["ID"],
            childColumns = ["MaPhong"],
            onDelete = ForeignKey.SET_NULL,
        )
    ]
)
data class HopDongEntity(
    @PrimaryKey
    @ColumnInfo(name = "ID")
    val id: String,

    @ColumnInfo(name = "Ten")
    val ten: String?,

    @ColumnInfo(name = "ThoiHan")
    val thoiHan: Int?,

    @ColumnInfo(name = "NgayLapHopDong")
    val ngayLapHopDong: String?,

    @ColumnInfo(name = "NgayBatDau")
    val ngayBatDau: String?,

    @ColumnInfo(name = "NgayKetThuc")
    val ngayKetThuc: String?,

    @ColumnInfo(name = "TienCoc")
    val tienCoc: String?,

    @ColumnInfo(name = "TrangThai")
    val trangThai: Int?,

    @ColumnInfo(name = "HieuLuc")
    val hieuLuc: Int?,

    @ColumnInfo(name = "MaPhong")
    val maPhong: String? = null,

    @ColumnInfo(name = "MaKhach")
    val maKhach: String? = null,

    @ColumnInfo(name = "GhiChu")
    val ghiChu: String?
) {
    companion object {
        const val STATE_ACTIVE = 1
        const val STATE_INACTIVE = 0

        const val ACTIVE = 1
        const val INACTIVE = 0
    }

    fun toModel() = Contract(
        id = id,
        name = ten,
        duration = thoiHan,
        createdDate = ngayLapHopDong,
        startDate = ngayBatDau,
        endDate = ngayKetThuc,
        deposit = tienCoc,
        status = trangThai,
        isActive = hieuLuc,
        roomId = maPhong,
        customerId = maKhach,
        note = ghiChu
    )
}

