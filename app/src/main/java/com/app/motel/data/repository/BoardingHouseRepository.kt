package com.app.motel.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.app.motel.common.AppConstants
import com.app.motel.data.entity.KhuTroEntity
import com.app.motel.data.local.BillDAO
import com.app.motel.data.local.BoardingHouseDAO
import com.app.motel.data.local.ComplaintDAO
import com.app.motel.data.local.ContractDAO
import com.app.motel.data.local.NotificationDAO
import com.app.motel.data.local.RoomDAO
import com.app.motel.data.local.RulesDAO
import com.app.motel.data.local.ServiceDAO
import com.app.motel.data.local.TenantDAO
import com.app.motel.data.local.UserDAO
import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.network.ApiMock
import javax.inject.Inject

class BoardingHouseRepository @Inject constructor(
    private val boardingHouseDAO: BoardingHouseDAO,
    private val roomDAO: RoomDAO,
    private val prefs: SharedPreferences,
    private val serviceDAO: ServiceDAO,
    private val billDAO: BillDAO,
    private val contractDAO: ContractDAO,
    private val complaintDAO: ComplaintDAO,
    private val rulesDAO: RulesDAO,
    private val tenantDAO: TenantDAO,
    private val notificationDAO: NotificationDAO,
) {

    suspend fun createBoardingHouse(boardingHouse: BoardingHouse): Resource<BoardingHouse>{
        return try {
            val boardingEntity = boardingHouse.toCreateEntity()
            boardingHouseDAO.insert(boardingEntity)
            Resource.Success(boardingEntity.toModel())
        }catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }

    suspend fun updateBoardingHouse(boardingHouse: BoardingHouse): Resource<BoardingHouse>{
        return try {
            val boardingEntity = boardingHouse.toEntity()
            boardingHouseDAO.update(boardingEntity)
            Resource.Success(boardingEntity.toModel())
        }catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }

    suspend fun deleteBoardingHouse(boardingHouse: BoardingHouse): Resource<BoardingHouse>{
        return try {
            val boardingEntity = boardingHouse.toEntity()
            deleteAllRelateToBoardingHouse(boardingEntity.id)
            boardingHouseDAO.delete(boardingEntity)
            Resource.Success(boardingEntity.toModel(), message = "Xóa khu trọ thành công")
        }catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }

    suspend fun getBoardingHouseById(id: String): Resource<BoardingHouse>{
        return try {
            val boardingHouseEntity: KhuTroEntity? = boardingHouseDAO.getById(id)
            Resource.Success(boardingHouseEntity?.toModel())
        }catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }

    suspend fun getBoardingHouseByUserId(userId: String): Resource<List<BoardingHouse>>{
        return try {
            val boardingHouse = boardingHouseDAO.getByUserId(userId).map {
                it.toModel().apply {
                    rooms = roomDAO.getPhongsByKhuTroId(id).map { it.toModel() }
                }
            }
            Resource.Success(boardingHouse)
        }catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }

    suspend fun getCurrentBoardingHouse(userId: String): BoardingHouse?{
        val userBoardingHouseKey = AppConstants.BOARDING_HOUSE_ID_KEY + userId

        val currentBoardingHouse: String = prefs.getString(userBoardingHouseKey, "") ?: ""
        val boardingHouse: List<KhuTroEntity> = boardingHouseDAO.getByUserId(userId)

        return (boardingHouse.firstOrNull{ it.id == currentBoardingHouse }
            ?: boardingHouse.firstOrNull())?.toModel()?.apply {
                prefs.edit().putString(userBoardingHouseKey, id).apply()
            }
    }

    fun setCurrentBoardingHouse(userId: String, boardingHouse: BoardingHouse){
        val userBoardingHouseKey = AppConstants.BOARDING_HOUSE_ID_KEY + userId

        prefs.edit().putString(userBoardingHouseKey, boardingHouse.id).apply()
    }

    suspend fun deleteAllRelateToBoardingHouse(boardingHouseId: String): Boolean {
        return try{
            roomDAO.getPhongsByKhuTroId(boardingHouseId).forEach { phongEntity ->
                tenantDAO.updateRentByRoomId(phongEntity.id)
                billDAO.deleteByRoomId(phongEntity.id)
                contractDAO.deleteByRoomId(phongEntity.id)
                complaintDAO.deleteByRoomId(phongEntity.id)
            }

            rulesDAO.deleteByBoardingHouseId(boardingHouseId)
            notificationDAO.deleteByBoardingHouseId(boardingHouseId)
            serviceDAO.deleteByBoardingHouseId(boardingHouseId)
            roomDAO.deleteByBoardingHouseId(boardingHouseId)
            true
        }catch (e: Exception){
            Log.e("deleteRoomByBoardingHouseId", "deleteRoomByBoardingHouseId ${e.toString()}")
            false
        }
    }

}