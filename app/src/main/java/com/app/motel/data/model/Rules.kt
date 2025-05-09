package com.app.motel.data.model

import com.app.motel.common.service.DateConverter
import com.app.motel.common.service.IDManager
import com.app.motel.data.entity.QuyDinhEntity

data class Rules(
    val id: String = "",
    val title: String,
    val content: String?,
    val createdDate: String? = "",
    val boardingHouseId: String? = null,
    val status: Int = 1
){
    fun toEntity() = QuyDinhEntity(
        id = id,
        title = title,
        content = content,
        createdDate = createdDate,
        khuTroId = boardingHouseId,
        status = status
    )

    fun toEntityCreate() = QuyDinhEntity(
        id = IDManager.createID(),
        title = title,
        content = content,
        createdDate = DateConverter.getCurrentLocalDateTime(),
        khuTroId = boardingHouseId,
        status = QuyDinhEntity.STATUS_ACTIVE
    )
}
