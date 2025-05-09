package com.app.motel.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class HoaDonWithPhong(
    @Embedded val hoaDon: HoaDonEntity,

    @Relation(
        parentColumn = "MaPhong",
        entityColumn = "ID"
    )
    val phong: PhongEntity?
)
