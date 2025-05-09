package com.app.motel.data.repository

import com.app.motel.data.entity.NguoiThueEntity
import com.app.motel.data.local.ContractDAO
import com.app.motel.data.local.RoomDAO
import com.app.motel.data.local.TenantDAO
import com.app.motel.data.model.CommonUser
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.model.Tenant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TenantRepository @Inject constructor(
    private val tenantDAO: TenantDAO,
    private val roomDAO: RoomDAO,
    private val contractDAO: ContractDAO,
) {
    private suspend fun fetchTenantData(tenant: Tenant): Tenant {
        if(tenant.roomId != null ) {
            tenant.room = roomDAO.getPhongById(tenant.roomId)?.toModel()
            tenant.contract = contractDAO.getByRoomTenantId(tenant.roomId, tenant.id).firstOrNull()?.toModel()
        }
        return tenant
    }

    suspend fun getTenants(): Resource<List<Tenant>> {
        return try {
            Resource.Success(tenantDAO.getTenants().map {
                fetchTenantData(it.toModel())
            })
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "Unknown error")
        }
    }

    suspend fun getTenantsByLandlordId(landlordId: String): List<Tenant> {
        return tenantDAO.getTenantsByLandlordId(landlordId).map {
            fetchTenantData(it.toModel())
        }
    }

    // Add this new function to get tenants by boarding house ID
    suspend fun getTenantsByBoardingHouseId(boardingHouseId: String): List<Tenant> {
        return tenantDAO.getTenantsByBoardingHouseId(boardingHouseId).map {
            fetchTenantData(it.toModel())
        }
    }

    suspend fun getAvailableRoomsForTenant(landlordId: String): Resource<List<Room>> {
        return try {
            val roomEntities = roomDAO.getAvailableRoomsByLandlordId(landlordId)
            val rooms = roomEntities.map { it.toModel() }
            Resource.Success(rooms)
        } catch (e: Exception) {
            Resource.Error(message = e.message ?: "Không thể tải danh sách phòng")
        }
    }

    suspend fun getAvailableTenantsForContract(landlordId: String, boardingHouseId: String?): List<Tenant> {
        // Get tenants that belong to this landlord, this boarding house, and are not locked
        val tenantEntities = if (boardingHouseId != null) {
            tenantDAO.getTenantsByLandlordAndBoardingHouse(landlordId, boardingHouseId)
                .filter { !it.biKhoa }
        } else {
            tenantDAO.getTenantsByLandlordId(landlordId)
                .filter { !it.biKhoa }
        }

        return tenantEntities.map { fetchTenantData(it.toModel()) }
    }

    suspend fun getTenantsByRoomId(roomId: String?): List<Tenant> {
        return tenantDAO.getTenantByRoomId(roomId).map { it.toModel() }
    }

    suspend fun getTenantsById(id: String): Tenant? {
        return tenantDAO.getNguoiThueById(id)?.toModel()
    }

    suspend fun updateTenant(tenant: Tenant): Resource<Tenant> {
        try {
            val tenantEntity = tenant.toEntity()
            tenantDAO.update(tenantEntity)
            return Resource.Success(tenantEntity.toModel(), message = "Cập nhật thành công")
        } catch (e: Exception) {
            return Resource.Error(message = e.message ?: "Unknown error")
        }
    }

    suspend fun addTenant(tenant: Tenant): Resource<Tenant>{
        return try {
            val tenantEntity = tenant.toEntityCreate()

            if (tenantDAO.getUserByUsername(tenantEntity.tenDangNhap) != null) {
                return Resource.Error(message = "Tên đăng nhập đã tồn tại")
            }
            tenantDAO.insert(tenantEntity)
            Resource.Success(tenantEntity.toModel(), message = "Thêm thành công")
        }catch (e: Exception) {
            Resource.Error(message = e.message ?: "Unknown error")
        }
    }

    suspend fun updateTenantLockStatus(tenant: Tenant, lockStatus: Boolean): Resource<Tenant> {
        return withContext(Dispatchers.IO) {
            try {
                // Update the lock status
                tenantDAO.updateLockStatus(tenant.id, lockStatus)

                // If locking the tenant, update the status to "Đã chuyển đi"
                if (lockStatus) {
                    tenantDAO.updateTenantStatus(tenant.id, NguoiThueEntity.Status.TEMPORARY_ABSENT.value)
                }
                // If unlocking the tenant, determine the appropriate status
                else {
                    // Check if tenant has a room assigned
                    val hasRoom = tenantDAO.getTenantById(tenant.id)?.maPhong != null

                    // Set status based on room assignment
                    val newStatus = if (hasRoom) {
                        NguoiThueEntity.Status.ACTIVE.value  // "Đang thuê"
                    } else {
                        NguoiThueEntity.Status.INACTIVE.value  // "Người thuê mới"
                    }

                    tenantDAO.updateTenantStatus(tenant.id, newStatus)
                }

                // Reload the tenant to get the updated data
                val updatedTenant = tenantDAO.getTenantById(tenant.id)?.toModel()
                    ?: return@withContext Resource.Error(message = "Không tìm thấy thông tin người thuê")

                Resource.Success(
                    updatedTenant,
                    message = if(lockStatus) "Khóa người thuê thành công" else "Mở khóa người thuê thành công"
                )
            } catch (e: Exception) {
                Resource.Error(message = e.message ?: "Cập nhật trạng thái khóa thất bại")
            }
        }
    }

    // In TenantRepository.kt
    suspend fun updateTenantRentToRoom(tenantId: String, roomId: String?): Resource<Tenant> {
        return withContext(Dispatchers.IO) {
            try {
                // Update the tenant's room assignment in the database
                tenantDAO.updateTenantRoom(tenantId, roomId)

                // Update tenant status based on room assignment
                if (roomId != null) {
                    // If assigned to a room, update status to "Đang thuê"
                    tenantDAO.updateTenantStatus(tenantId, NguoiThueEntity.Status.ACTIVE.value)
                } else {
                    // If removed from a room, update status to "Người thuê mới"
                    tenantDAO.updateTenantStatus(tenantId, NguoiThueEntity.Status.INACTIVE.value)
                }

                // Fetch the updated tenant to return
                val updatedTenant = tenantDAO.getTenantById(tenantId)?.toModel()
                    ?: return@withContext Resource.Error(data = null, message = "Không tìm thấy thông tin người thuê")

                Resource.Success(
                    updatedTenant,
                    message = if (roomId != null) "Chuyển phòng thành công" else "Chuyển người thuê ra khỏi phòng thành công"
                )
            } catch (e: Exception) {
                Resource.Error(data = null, message = e.message ?: "Cập nhật phòng cho người thuê thất bại")
            }
        }
    }
    suspend fun removeTenantFromRoom(roomId: String): Resource<List<Tenant>?>{
        return try {
            tenantDAO.updateRentByRoomId(roomId)
            Resource.Success(data = null, message = "Cập nhật thành công")
        }catch (e: Exception) {
            Resource.Error(message = e.toString())
        }
    }

    suspend fun deleteTenant(tenantId: String): Resource<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                tenantDAO.deleteTenantById(tenantId)
                Resource.Success(true, message = "Xóa khách thuê thành công")
            } catch (e: Exception) {
                Resource.Error(message = e.message ?: "Xóa khách thuê thất bại")
            }
        }
    }
    // Update your getTenantsByFilter method in TenantRepository.kt
    suspend fun getTenantsByFilter(filterType: TenantFilterType): List<Tenant> {
        return when (filterType) {
            TenantFilterType.ALL -> tenantDAO.getTenants()
            TenantFilterType.ACTIVE -> tenantDAO.getTenantsByStatusAndLock(
                NguoiThueEntity.Status.ACTIVE.value, false
            )
            TenantFilterType.INACTIVE -> tenantDAO.getTenantsByStatusOrLock(
                NguoiThueEntity.Status.TEMPORARY_ABSENT.value, true
            )
        }.map { fetchTenantData(it.toModel()) }
    }

    suspend fun getTenantsByFilterAndLandlord(filterType: TenantFilterType, landlordId: String): List<Tenant> {
        // First get all tenants for this landlord
        val landlordTenants = tenantDAO.getTenantsByLandlordId(landlordId)

        // Then apply the filter
        val filteredTenants = when (filterType) {
            TenantFilterType.ALL -> landlordTenants
            TenantFilterType.ACTIVE -> landlordTenants.filter {
                it.trangThai == NguoiThueEntity.Status.ACTIVE.value && !it.biKhoa
            }
            TenantFilterType.INACTIVE -> landlordTenants.filter {
                it.trangThai == NguoiThueEntity.Status.TEMPORARY_ABSENT.value || it.biKhoa
            }
        }

        return filteredTenants.map { fetchTenantData(it.toModel()) }
    }

}