package com.app.motel.data.model

import com.app.motel.common.service.DateConverter
import com.app.motel.common.service.IDManager
import com.app.motel.data.entity.KhieuNaiEntity
import com.app.motel.data.local.TenantDAO

data class Complaint(
    val id: String = "",
    val title: String,
    val content: String?,
    val createdDate: String? = null,
    val submittedBy: String?,
    val roomId: String?,
    val status: String? = null,
    val type: Int? = null,
){
    var room: Room? = null
    var tenant: Tenant? = null

    val isRentRoom get() = type == KhieuNaiEntity.Type.RENT_ROOM.value
    val isComplaint get() = type == KhieuNaiEntity.Type.COMPLAINT.value

    fun toEntity() = KhieuNaiEntity(
        id = id,
        title = title,
        content = content,
        createdDate = createdDate,
        submittedBy = submittedBy,
        roomId = roomId,
        type = type,
        status = status
    )

    fun toEntityCreateComplaint() = KhieuNaiEntity(
        id = IDManager.createID(),
        title = title,
        content = content,
        createdDate = DateConverter.getCurrentStringDateTime(),
        submittedBy = submittedBy,
        roomId = roomId,
        status = KhieuNaiEntity.Status.NEW.value,
        type = KhieuNaiEntity.Type.COMPLAINT.value,
    )

    fun toEntityCreateRentRoom() = KhieuNaiEntity(
        id = IDManager.createID(),
        title = title,
        content = content,
        createdDate = DateConverter.getCurrentStringDateTime(),
        submittedBy = submittedBy,
        roomId = roomId,
        status = KhieuNaiEntity.Status.NEW.value,
        type = KhieuNaiEntity.Type.RENT_ROOM.value,
    )
}
