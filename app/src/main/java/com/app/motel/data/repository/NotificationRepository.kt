package com.app.motel.data.repository

import android.util.Log
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

    // Đếm số thông báo chưa đọc cho người thuê
    suspend fun countUnreadNotificationsForTenant(tenantId: String): Int {
        return try {
            val count = notificationDAO.countUnreadNotificationsForTenant(tenantId)
            Log.d("NotificationRepository", "Đếm thông báo chưa đọc cho người thuê $tenantId: $count")
            count
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Lỗi đếm thông báo chưa đọc: ${e.message}", e)
            0
        }
    }

    // Đánh dấu tất cả thông báo là đã đọc cho người thuê
    suspend fun markAllNotificationsAsReadForTenant(tenantId: String): Int {
        return try {
            val count = notificationDAO.markAllNotificationsAsReadForTenant(tenantId)
            Log.d("NotificationRepository", "Đã đánh dấu $count thông báo là đã đọc cho người thuê $tenantId")
            count
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Lỗi đánh dấu thông báo đã đọc: ${e.message}", e)
            0
        }
    }

    // Đếm số thông báo chung chưa đọc cho người thuê
    suspend fun countUnreadGeneralNotificationsForTenant(tenantId: String): Int {
        return try {
            notificationDAO.countUnreadGeneralNotificationsForTenant(tenantId)
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Lỗi đếm thông báo chung chưa đọc: ${e.message}", e)
            0
        }
    }

    // Đếm số thông báo riêng chưa đọc cho người thuê
    suspend fun countUnreadRoomNotificationsForTenant(tenantId: String): Int {
        return try {
            notificationDAO.countUnreadRoomNotificationsForTenant(tenantId)
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Lỗi đếm thông báo riêng chưa đọc: ${e.message}", e)
            0
        }
    }

    // Đánh dấu một thông báo là đã đọc
    suspend fun markNotificationAsRead(notificationId: String): Boolean {
        return try {
            notificationDAO.markAsRead(notificationId)
            true
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Lỗi đánh dấu thông báo đã đọc: ${e.message}", e)
            false
        }
    }
}