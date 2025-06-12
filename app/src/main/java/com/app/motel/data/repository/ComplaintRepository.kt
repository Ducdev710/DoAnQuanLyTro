package com.app.motel.data.repository

import android.util.Log
import com.app.motel.data.entity.KhieuNaiEntity
import com.app.motel.data.local.ComplaintDAO
import com.app.motel.data.local.RoomDAO
import com.app.motel.data.local.TenantDAO
import com.app.motel.data.model.Complaint
import com.app.motel.data.model.Resource
import javax.inject.Inject

class ComplaintRepository @Inject constructor(
    private val complaintDAO: ComplaintDAO,
    private val roomDAO: RoomDAO,
    private val tenantDAO: TenantDAO,
) {

    //Lấy danh sách khiếu nại theo nhà trọ kèm thông tin phòng và người thuê
    suspend fun getComplaintAdmin(boardingHouseId: String): List<Complaint>{
        return complaintDAO.getByBoardingHouseId(boardingHouseId).map {
            it.toModel().apply {
                room = roomDAO.getPhongById(it.roomId ?: "")?.toModel()
                tenant = tenantDAO.getById(it.submittedBy ?: "")?.toModel()
            }
        }
    }

    //Lấy danh sách khiếu nại của người thuê kèm thông tin phòng
    suspend fun getComplainByUser(tenantId: String): List<Complaint>{
        return complaintDAO.getByTenantId(tenantId).map {
            it.toModel().apply {
                room = roomDAO.getPhongById(it.roomId ?: "")?.toModel()
            }
        }
    }

    //Tạo yêu cầu thuê phòng
    suspend fun createRequireRentRoom(complaint: Complaint): Resource<Complaint>{
        return try {
            val entity = complaint.toEntityCreateRentRoom()
            complaintDAO.insertComplaint(entity)
            Resource.Success(entity.toModel())
        }catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }

    suspend fun createComplaint(complaint: Complaint): Resource<Complaint>{
        return try {
            val entity = complaint.toEntityCreateComplaint()
            Log.e("ComplaintRepository", "createComplaint: ${entity}")
            complaintDAO.insertComplaint(entity)
            Resource.Success(entity.toModel())
        }catch (e: Exception){
            Log.e("ComplaintRepository", "createComplaint: loi ${e}")
            Resource.Error(message = e.toString())
        }
    }

    //Tạo thông báo hệ thống về thanh toán hóa đơn
    suspend fun createBillPaymentNotification(complaint: Complaint): Resource<Complaint> {
        return try {
            // Use the system notification entity transformation
            val entity = complaint.toEntityCreateSystemNotification()
            Log.d("ComplaintRepository", "Creating bill payment notification: ${entity}")
            complaintDAO.insertComplaint(entity)
            Resource.Success(entity.toModel())
        } catch (e: Exception) {
            Log.e("ComplaintRepository", "Error creating payment notification: ${e.message}", e)
            Resource.Error(message = e.toString())
        }
    }

    //Cập nhật trạng thái xử lý khiếu nại
    //Trả về thông tin khiếu nại sau khi cập nhật
    suspend fun updateStateComplaint(id: String, state: String): Resource<Complaint> {
        return try {
            complaintDAO.updateStateComplaint(id, state)
            Resource.Success(complaintDAO.getComplaintById(id)?.toModel())
        }catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }

    // Lấy số lượng thông báo mới
    suspend fun getNewNotificationsCount(): Int {
        return try {
            complaintDAO.countNewNotifications()
        } catch (e: Exception) {
            Log.e("ComplaintRepository", "Error getting new notifications count: ${e.message}", e)
            0
        }
    }

    // Lấy số lượng thông báo mới cho admin theo boarding house
    suspend fun getNewNotificationsForAdmin(boardingHouseId: String): Int {
        return try {
            complaintDAO.countNewNotificationsForAdmin(boardingHouseId)
        } catch (e: Exception) {
            Log.e("ComplaintRepository", "Error getting admin notifications: ${e.message}", e)
            0
        }
    }

    // Lấy số lượng thông báo mới theo loại
    suspend fun getNewNotificationsByType(type: Int): Int {
        return try {
            complaintDAO.countNewNotificationsByType(type)
        } catch (e: Exception) {
            Log.e("ComplaintRepository", "Error getting notifications by type $type: ${e.message}", e)
            0
        }
    }

    // Lấy tất cả thông báo có trạng thái Mới
    suspend fun getNewNotifications(): List<Complaint> {
        return try {
            complaintDAO.getComplaintsByStatus(KhieuNaiEntity.Status.NEW.value).map { it.toModel() }
        } catch (e: Exception) {
            Log.e("ComplaintRepository", "Error getting new notifications: ${e.message}", e)
            emptyList()
        }
    }

    // Cập nhật tất cả thông báo ứng dụng sang trạng thái đã xử lý
    suspend fun updateAllApplicationNotifications(boardingHouseId: String, newStatus: String): Int {
        return try {
            // Gọi DAO để cập nhật tất cả thông báo APPLICATION từ trạng thái "Mới" sang newStatus
            val count = complaintDAO.updateAllApplicationNotifications(boardingHouseId, newStatus)
            Log.d("ComplaintRepository", "Đã cập nhật $count thông báo ứng dụng sang trạng thái $newStatus")
            count
        } catch (e: Exception) {
            Log.e("ComplaintRepository", "Lỗi cập nhật tất cả thông báo ứng dụng: ${e.message}", e)
            0
        }
    }

    // Kiểm tra xem có thông báo ứng dụng mới không
    suspend fun hasNewApplicationNotifications(boardingHouseId: String): Boolean {
        return try {
            val count = complaintDAO.countNewApplicationNotifications(boardingHouseId)
            count > 0
        } catch (e: Exception) {
            Log.e("ComplaintRepository", "Lỗi kiểm tra thông báo ứng dụng mới: ${e.message}", e)
            false
        }
    }
}