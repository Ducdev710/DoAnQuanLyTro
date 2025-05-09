package com.app.motel.data.entity

import androidx.room.*
import com.app.motel.data.model.Complaint

@Entity(
    tableName = "KhieuNai",
    foreignKeys = [
        ForeignKey(
            entity = PhongEntity::class,
            parentColumns = ["ID"],
            childColumns = ["MaPhong"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = NguoiThueEntity::class,
            parentColumns = ["ID"],
            childColumns = ["NguoiNop"],
            onDelete = ForeignKey.SET_NULL,
        )
    ],
)
data class KhieuNaiEntity(
    @PrimaryKey
    @ColumnInfo(name = "ID")
    val id: String,

    @ColumnInfo(name = "TieuDe")
    val title: String,

    @ColumnInfo(name = "NoiDung")
    val content: String?,

    @ColumnInfo(name = "NgayTao")
    val createdDate: String?,

    @ColumnInfo(name = "NguoiNop")
    val submittedBy: String? = null,

    @ColumnInfo(name = "MaPhong")
    val roomId: String? = null,

    @ColumnInfo(name = "TrangThai")
    val status: String?,

    @ColumnInfo(name = "KieuKhieuNai")
    val type: Int?,
) {
    enum class Status(val value: String) {
        NEW("Mới"),
        PENDING("Đang xử lý"),
        RESOLVED("Đã xử lý"),
        REJECTED("Từ chối"),;

        companion object {
            fun fromValue(value: String) = when(value) {
                NEW.value -> NEW
                PENDING.value -> PENDING
                RESOLVED.value -> RESOLVED
                REJECTED.value -> REJECTED
                else -> NEW
            }
        }
    }
    enum class Type(val value: Int) {
        APPLICATION(999),
        COMPLAINT(0),
        RENT_ROOM(1),;

        companion object {
            fun fromValue(value: Int) = when(value) {
                COMPLAINT.value -> COMPLAINT
                RENT_ROOM.value -> RENT_ROOM
                else -> COMPLAINT
            }
        }
    }

    fun toModel() = Complaint(
        id = id,
        title = title,
        content = content,
        createdDate = createdDate,
        submittedBy = submittedBy,
        roomId = roomId,
        status = status,
        type = type
    )
}
