package com.app.motel.data.model

import com.app.motel.common.service.DateConverter
import com.app.motel.common.service.IDManager
import com.app.motel.data.entity.ThongBaoEntity

data class Notification(
    val id: String = "",
    val title: String,
    val content: String?,
    val createdDate: String? = null,
    val khuTroId: String?,
    val phongId: String?,
    val isRead: Int = 0
) {
    var room: Room? = null

    fun toEntity() = ThongBaoEntity(
        id = id,
        title = title,
        content = content,
        createdDate = createdDate,
        khuTroId = khuTroId,
        phongId = phongId,
        isRead = isRead
    )

    fun toEntityInsert() = ThongBaoEntity(
        id = IDManager.createID(),
        title = title,
        content = content,
        createdDate = DateConverter.getCurrentStringDateTime(),
        khuTroId = khuTroId,
        phongId = phongId,
        isRead = isRead
    )
}
