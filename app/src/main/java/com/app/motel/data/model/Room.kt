package com.app.motel.data.model

import com.app.motel.common.service.IDManager
import com.app.motel.data.entity.PhongEntity

data class Room(
    val id: String = "",
    val roomName: String,
    val maxOccupants: Int? = null,
    val area: Double? = null,
    val rentalPrice: String = "",
    var services: String? = null,
    val status: String? = null,
    val areaId: String? = null
    ) {

    val isEmpty get() = status == PhongEntity.Status.EMPTY.value
    val isRenting get() = status == PhongEntity.Status.RENTED.value

    var tenants: List<Tenant>? = null
    var listService: List<Service>? = null
    var contract: Contract? = null
    @Transient
    var boardingHouse: BoardingHouse? = null

    fun toEntity(): PhongEntity {
        return PhongEntity(
            id = id,
            tenPhong = roomName,
            soNguoiToiDa = maxOccupants,
            dienTich = area,
            giaThue = rentalPrice,
            dichVu = services,
            trangThai = status,
            maKhuTro = areaId
        )
    }

    fun toCreateEntity(): PhongEntity {
        return PhongEntity(
            id = IDManager.createID(),
            tenPhong = roomName,
            soNguoiToiDa = maxOccupants,
            dienTich = area,
            giaThue = rentalPrice,
            dichVu = services,
            trangThai = PhongEntity.Status.EMPTY.value,
            maKhuTro = areaId
        )
    }

    override fun toString(): String = roomName
}