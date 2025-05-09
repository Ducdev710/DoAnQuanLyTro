package com.app.motel.data.repository

import com.app.motel.data.local.BoardingHouseDAO
import com.app.motel.data.local.NotificationDAO
import com.app.motel.data.local.RoomDAO
import com.app.motel.data.model.Notification
import com.app.motel.data.model.Resource
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val roomDAO: RoomDAO,
    private val boardingHouseDAO: BoardingHouseDAO,
    private val notificationDAO: NotificationDAO,
) {

    suspend fun getNotificationByBoardingHouseId(boardingHouseId: String): List<Notification>{
        return notificationDAO.getAdminNotification(boardingHouseId).map {
            it.toModel().apply {
                room = roomDAO.getPhongById(it.phongId ?: "")?.toModel()
            }
        }
    }

    suspend fun getNotificationByTenantId(tenantId: String): List<Notification>{
//        val currentBoardingHouseEntities = boardingHouseDAO.getByTenantId(tenantId)
//        return currentBoardingHouseEntities.flatMap { boardingHouseEntity ->
//            getNotificationByBoardingHouseId(boardingHouseEntity.id)
//        }
        return notificationDAO.getUserNotification(tenantId).map {
            it.toModel().apply {
                room = roomDAO.getPhongById(it.phongId ?: "")?.toModel()
            }
        }
    }

    suspend fun addNews(notification: Notification): Resource<Notification>{
        return try {
            val notificationEntity = notification.toEntityInsert()
            notificationDAO.insert(notificationEntity)
            Resource.Success(notificationEntity.toModel())
        }catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }
}