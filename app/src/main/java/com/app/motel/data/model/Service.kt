package com.app.motel.data.model

import com.app.motel.common.service.IDManager
import com.app.motel.common.utils.toStringMoney
import com.app.motel.data.entity.DichVuEntity

data class Service(
    val id: String = "",
    val name: String,
    val price: String,
    val typePay: String?,
    val areaId: String?,
    val roomId: String?
) {
    val isAppliesAllRoom: Boolean get() = roomId == null

    val getPriceTypePay get() = "${price.toStringMoney()} đồng/${(typePay?.replace("Theo", " ")?.trim() ?: "")}"

    fun toEntity(): DichVuEntity {
        return DichVuEntity(
            id = id,
            tenDichVu = name,
            giaDichVu = price,
            loaiHinhThanhToan = typePay,
            maKhuTro = areaId,
            maPhong = roomId
        )
    }

    fun toCreateEntity(): DichVuEntity {
        return DichVuEntity(
            id = IDManager.createID(),
            tenDichVu = name,
            giaDichVu = price,
            loaiHinhThanhToan = typePay,
            maKhuTro = areaId,
            maPhong = roomId
        )
    }
}
