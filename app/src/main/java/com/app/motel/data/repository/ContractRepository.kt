package com.app.motel.data.repository

import android.util.Log
import com.app.motel.data.entity.HopDongEntity
import com.app.motel.data.entity.NguoiThueEntity
import com.app.motel.data.local.ContractDAO
import com.app.motel.data.local.RoomDAO
import com.app.motel.data.local.TenantDAO
import com.app.motel.data.model.Contract
import com.app.motel.data.model.Resource
import javax.inject.Inject

class ContractRepository @Inject constructor(
    private val roomDAO: RoomDAO,
    private val contractDAO: ContractDAO,
    private val tenantDAO: TenantDAO,
) {

    suspend fun getContractByBoardingHouseId(boardingHouseId: String): List<Contract> {
        val contractEntities: List<HopDongEntity> = contractDAO.getContractsByBoardingId(boardingHouseId)
        return contractEntities.map { contractEntity ->
            val roomEntity = roomDAO.getPhongById(contractEntity.maPhong ?: "")
            val tenantEntity = tenantDAO.getNguoiThueById(contractEntity.maKhach ?: "")
            contractEntity.toModel().apply {
                this.room = roomEntity?.toModel()
                this.tenant = tenantEntity?.toModel()
            }
        }
    }

    suspend fun getContractByTenantId(tenantId: String): List<Contract> {
        val contractEntities: List<HopDongEntity> = contractDAO.getByTenantId(tenantId)
        return contractEntities.map { contractEntity ->
            val roomEntity = roomDAO.getPhongById(contractEntity.maPhong ?: "")
            val tenantEntity = tenantDAO.getNguoiThueById(contractEntity.maKhach ?: "")
            contractEntity.toModel().apply {
                this.room = roomEntity?.toModel()
                this.tenant = tenantEntity?.toModel()
            }
        }
    }

    suspend fun getContractActiveByTenantId(tenantId: String): Contract? {
        val contractEntities: List<HopDongEntity> = contractDAO.getByTenantId(tenantId)
        Log.e("ContractRepository", "contractEntities: ${contractEntities}")
        return contractEntities.firstOrNull{ contractEntity ->
            contractEntity.hieuLuc == HopDongEntity.ACTIVE
        }?.toModel()
    }

    suspend fun getContractActiveByRoomId(roomId: String): Contract? {
        val contractEntities: List<HopDongEntity> = contractDAO.getByRoomId(roomId)
        return contractEntities.firstOrNull{ contractEntity ->
            contractEntity.hieuLuc == HopDongEntity.ACTIVE
        }?.toModel()
    }

    suspend fun createContract(contract: Contract): Resource<Contract>{
        return try {
            val contractEntity = contract.toCreateEntity()
            val id = contractDAO.insert(contractEntity)

            // Update tenant status to ACTIVE and mark as contract holder
            if (contract.customerId != null) {
                tenantDAO.updateTenantStatus(contract.customerId, NguoiThueEntity.Status.ACTIVE.value)
                tenantDAO.updateIsContractHolder(contract.customerId, true)
                tenantDAO.updateRentedRoom(contract.customerId, contract.roomId ?: "")
            }

            Resource.Success(contractEntity.toModel(), message = "Tạo hợp đồng thành công")
        } catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }

    suspend fun updateContract(contract: Contract): Resource<Contract> {
        return try {
            val contractEntity = contract.toEntity()
            contractDAO.update(contractEntity)
            Resource.Success(contractEntity.toModel(), message = "Sửa hợp đồng thành công")
        }catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }

    suspend fun updateStateRoom(roomId: String, state: String): Resource<Boolean>{
        return try {
            roomDAO.updateStatus(roomId, state)
            Resource.Success(true)
        }catch (e: Exception) {
            Resource.Error(message = e.toString())
        }
    }
}