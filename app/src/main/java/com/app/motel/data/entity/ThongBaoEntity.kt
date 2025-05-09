package com.app.motel.data.entity

import androidx.room.*
import com.app.motel.data.model.Notification

@Entity(
    tableName = "ThongBao",
    foreignKeys = [
        ForeignKey(
            entity = KhuTroEntity::class,
            parentColumns = ["ID"],
            childColumns = ["MaKhuTro"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = PhongEntity::class,
            parentColumns = ["ID"],
            childColumns = ["MaPhong"],
            onDelete = ForeignKey.SET_NULL,
        )
    ],
//    indices = [Index(value = ["MaKhuTro"]), Index(value = ["MaPhong"])]
)
data class ThongBaoEntity(
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
    val khuTroId: String?,

    @ColumnInfo(name = "MaPhong")
    val phongId: String?,

    @ColumnInfo(name = "DaDoc")
    val isRead: Int? = 0
) {
    fun toModel() = Notification(
        id = id,
        title = title,
        content = content,
        createdDate = createdDate,
        khuTroId = khuTroId,
        phongId = phongId,
        isRead = isRead ?: 0
    )
}
