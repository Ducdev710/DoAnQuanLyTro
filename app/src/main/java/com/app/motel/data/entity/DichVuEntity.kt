package com.app.motel.data.entity

import androidx.room.*
import com.app.motel.data.model.Service

@Entity(
    tableName = "DichVu",
    foreignKeys = [
        ForeignKey(
            entity = PhongEntity::class,
            parentColumns = ["ID"],
            childColumns = ["MaPhong"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = KhuTroEntity::class,
            parentColumns = ["ID"],
            childColumns = ["MaKhuTro"],
            onDelete = ForeignKey.SET_NULL,
        )
    ]
)
data class DichVuEntity(
    @PrimaryKey
    @ColumnInfo(name = "ID")
    val id: String,

    @ColumnInfo(name = "TenDichVu")
    val tenDichVu: String,

    @ColumnInfo(name = "GiaDichVu")
    val giaDichVu: String,

    @ColumnInfo(name = "LoaiHinhThanhToan")
    val loaiHinhThanhToan: String? = null,

    @ColumnInfo(name = "MaKhuTro")
    val maKhuTro: String? = null,

    @ColumnInfo(name = "MaPhong")
    val maPhong: String? = null
) {
    enum class TypePay(val typeName: String){
        FREE("Miễn phí/không sử dụng"),
        TO_ROOM("Theo phòng"),
        TO_PERSON("Theo đầu người"),
        TO_METER("Theo đồng hồ");

        companion object{
            fun getType(name: String?): TypePay?{
                return when(name){
                    FREE.typeName -> FREE
                    TO_ROOM.typeName -> TO_ROOM
                    TO_PERSON.typeName -> TO_PERSON
                    TO_METER.typeName -> TO_METER
                    else -> null
                }
            }
        }
    }

    fun toModel(): Service {
        return Service(
            id = id,
            name = tenDichVu,
            price = giaDichVu,
            typePay = loaiHinhThanhToan,
            areaId = maKhuTro,
            roomId = maPhong
        )
    }
}
