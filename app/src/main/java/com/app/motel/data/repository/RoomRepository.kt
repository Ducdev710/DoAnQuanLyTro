package com.app.motel.data.repository

import android.util.Log
import androidx.room.Transaction
import com.app.motel.data.entity.KhuTroEntity
import com.app.motel.data.entity.PhongEntity
import com.app.motel.data.local.BillDAO
import com.app.motel.data.local.BoardingHouseDAO
import com.app.motel.data.local.ComplaintDAO
import com.app.motel.data.local.ContractDAO
import com.app.motel.data.local.NotificationDAO
import com.app.motel.data.local.RoomDAO
import com.app.motel.data.local.RulesDAO
import com.app.motel.data.local.ServiceDAO
import com.app.motel.data.local.TenantDAO
import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import javax.inject.Inject

class RoomRepository @Inject constructor(
    private val boardingHouseDAO: BoardingHouseDAO,
    private val roomDAO: RoomDAO,
    private val tenantRepository: TenantRepository,
) {

    suspend fun geRoomBytBoardingHouseId(boardingHouseId: String, state: PhongEntity.Status? = null): List<Room> {
        val roomEntities = roomDAO.getPhongsByKhuTroId(boardingHouseId, state?.value)
        return roomEntities.map {
            it.toModel().apply {
                tenants = tenantRepository.getTenantsByRoomId(this.id)
            }
        }
    }

    suspend fun getCurrentRoomRentByTenantId(tenantId: String, status: PhongEntity.Status? = null): List<Room> {
        val roomEntities = roomDAO.getCurrentRoomRentByTenantId(tenantId, status?.value)
        return roomEntities.map {
            it.toModel().apply {
                tenants = tenantRepository.getTenantsByRoomId(this.id)
            }
        }
    }

    suspend fun getRoomByStatus( status: PhongEntity.Status? = null): List<Room> {
        val roomEntities = roomDAO.getPhongsByStatus(status?.value)
        return roomEntities.map {
            it.toModel().apply {
                boardingHouse = boardingHouseDAO.getById(this.areaId ?: "")?.toModel()
                tenants = tenantRepository.getTenantsByRoomId(this.id)
            }
        }
    }

    suspend fun getAvailableRoomsByLandlordId(landlordId: String): List<Room> {
        val roomEntities = roomDAO.getAvailableRoomsByLandlordId(landlordId)
        return roomEntities.map {
            it.toModel().apply {
                boardingHouse = boardingHouseDAO.getById(this.areaId ?: "")?.toModel()
            }
        }
    }

    suspend fun getBoardingRoomById(boardingHouseId: String): BoardingHouse? {
        val boardingHousesEntity: KhuTroEntity? = boardingHouseDAO.getById(boardingHouseId)
        return boardingHousesEntity?.toModel().apply {
            this?.rooms = geRoomBytBoardingHouseId(boardingHouseId)
        }
    }

    suspend fun getRoomById(roomId: String): Room? {
        val roomEntity = roomDAO.getPhongById(roomId)
        return roomEntity?.toModel()
    }

    suspend fun createRoom(room: Room): Resource<Room>{
        return try {
            val roomEntity = room.toCreateEntity()
            roomDAO.insert(roomEntity)
            Resource.Success(roomEntity.toModel())
        }catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }

    suspend fun updateRoom(room: Room): Resource<Room> {
        return try {
            val roomEntity = room.toEntity()
            roomDAO.update(roomEntity)
            Resource.Success(roomEntity.toModel())
        }catch (e: Exception) {
            Resource.Error(message = e.toString())
        }
    }

    @Transaction
    suspend fun deleteRoom(room: Room): Resource<Room> {
        return try {
            val roomEntity = room.toEntity()

            roomDAO.delete(roomEntity)
            Resource.Success(roomEntity.toModel())
        }catch (e: Exception) {
            Resource.Error(message = e.toString())
        }
    }
}