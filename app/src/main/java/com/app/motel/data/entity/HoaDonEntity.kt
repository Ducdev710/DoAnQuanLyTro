package com.app.motel.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.app.motel.data.model.Bill

@Entity(
    tableName = "HoaDon",
    foreignKeys = [
        ForeignKey(
            entity = PhongEntity::class,
            parentColumns = ["ID"],
            childColumns = ["MaPhong"],
            onDelete = ForeignKey.SET_NULL,
        )
    ],
)
data class HoaDonEntity(
    @PrimaryKey
    @ColumnInfo(name = "ID")
    val id: String,

    @ColumnInfo(name = "Ten")
    val ten: String?,

    @ColumnInfo(name = "NgayTao")
    val createdDate: String?,

    @ColumnInfo(name = "GiaThue")
    val rentPrice: Double,

    @ColumnInfo(name = "SONUOC")
    val waterIndex: Int?,

    @ColumnInfo(name = "SoNuocTieuThu")
    val waterUsed: Int?,

    @ColumnInfo(name = "SODIEN")
    val electricityIndex: Int?,

    @ColumnInfo(name = "SoDienTieuThu")
    val electricityUsed: Int?,

    @ColumnInfo(name = "GiaDichVu")
    val serviceFee: String?,

    @ColumnInfo(name = "TienMienGiam")
    val discount: String? = "0",

    @ColumnInfo(name = "TongTien")
    val total: String?,

    @ColumnInfo(name = "TrangThai")
    val status: Int?,

    @ColumnInfo(name = "MaPhong")
    val roomId: String? = null,

    @ColumnInfo(name = "Thang")
    val month: Int,

    @ColumnInfo(name = "Nam")
    val year: Int,

    @ColumnInfo(name = "GhiChu")
    val note: String?
) {
    companion object{
        const val STATUS_UNPAID = 0
        const val STATUS_PAID = 1

        const val PRICE_WATER = 20000
        const val PRICE_ELECTRICITY = 3500
    }

    fun toModel(): Bill {
        return Bill(
            id = id,
            name = ten,
            createdDate = createdDate,
            roomPrice = rentPrice,
            waterIndex = waterIndex,
            waterUsed = waterUsed,
            electricityIndex = electricityIndex,
            electricityUsed = electricityUsed,
            serviceFee = serviceFee,
            discount = discount,
            totalAmount = total,
            status = status,
            roomId = roomId,
            month = month,
            year = year,
            note = note
        )
    }
}
