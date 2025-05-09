package com.app.motel.data.repository

import android.util.Log
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

    suspend fun getComplaintAdmin(boardingHouseId: String): List<Complaint>{
        return complaintDAO.getByBoardingHouseId(boardingHouseId).map {
            it.toModel().apply {
                room = roomDAO.getPhongById(it.roomId ?: "")?.toModel()
                tenant = tenantDAO.getById(it.submittedBy ?: "")?.toModel()
            }
        }
    }

    suspend fun getComplainByUser(tenantId: String): List<Complaint>{
        return complaintDAO.getByTenantId(tenantId).map {
            it.toModel().apply {
                room = roomDAO.getPhongById(it.roomId ?: "")?.toModel()
            }
        }
    }

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

    suspend fun updateStateComplaint(id: String, state: String): Resource<Complaint> {
        return try {
            complaintDAO.updateStateComplaint(id, state)
            Resource.Success(complaintDAO.getComplaintById(id)?.toModel())
        }catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }

}