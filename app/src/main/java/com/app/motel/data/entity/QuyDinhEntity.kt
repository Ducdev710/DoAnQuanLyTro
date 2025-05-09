package com.app.motel.data.entity

import androidx.room.*
import com.app.motel.data.model.Rules

@Entity(
    tableName = "QuyDinh",
    foreignKeys = [
        ForeignKey(
            entity = KhuTroEntity::class,
            parentColumns = ["ID"],
            childColumns = ["MaKhuTro"],
            onDelete = ForeignKey.SET_NULL,
        )
    ],
//    indices = [Index(value = ["MaKhuTro"])]
)
data class QuyDinhEntity(
    @PrimaryKey
    @ColumnInfo(name = "ID")
    val id: String,

    @ColumnInfo(name = "TieuDe")
    val title: String,

    @ColumnInfo(name = "NoiDung")
    val content: String?,

    @ColumnInfo(name = "NgayTao")
    val createdDate: String?,

    @ColumnInfo(name = "MaKhuTro")
    val khuTroId: String? = null,

    @ColumnInfo(name = "TrangThai")
    val status: Int? = 1
) {
    companion object{
        const val STATUS_ACTIVE = 1
        const val STATUS_INACTIVE = 0
    }

    fun toModel() = Rules(
        id = id,
        title = title,
        content = content,
        createdDate = createdDate,
        boardingHouseId = khuTroId,
        status = status ?: 1
    )
}
