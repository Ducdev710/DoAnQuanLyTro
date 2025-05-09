package com.app.motel.data.model

import com.app.motel.common.AppConstants
import com.app.motel.common.service.IDManager
import com.app.motel.data.entity.KhuTroEntity

data class BoardingHouse(
    val id: String = "",
    val name: String,
    val address: String,
    val roomCount: Int?,
    val ownerId: String? = null,
    val isActive: Boolean = true,
    val description: String? = null
){
    var rooms: List<Room>? = null
    var service: List<Service>? = null
    var rules: ArrayList<Rules>? = null

    val getRoomEmpty get() = rooms?.filter { it.isEmpty}
    val getRoomRenting get() = rooms?.filter { it.isRenting}
    val getRoomNearEnd get() = rooms?.filter { it.contract?.isNearEnd == true}

    fun toEntity(): KhuTroEntity {
        return KhuTroEntity(
            id = id,
            tenKhuTro = name,
            diaChi = address,
            soLuongPhong = roomCount,
            maChuNha = ownerId,
            trangThai = if (isActive) KhuTroEntity.STATE_ACTIVE else KhuTroEntity.STATE_INACTIVE,
            moTa = description
        )
    }

    fun toCreateEntity(): KhuTroEntity {
        return KhuTroEntity(
            id = IDManager.createID(),
            tenKhuTro = name,
            diaChi = address,
            soLuongPhong = roomCount,
            maChuNha = ownerId,
            trangThai = KhuTroEntity.STATE_ACTIVE,
            moTa = description
        )
    }
}
