package com.app.motel.data.repository

import android.util.Log
import com.app.motel.data.entity.KhuTroEntity
import com.app.motel.data.local.BoardingHouseDAO
import com.app.motel.data.local.RoomDAO
import com.app.motel.data.local.ServiceDAO
import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.model.Service
import javax.inject.Inject

class ServiceRepository@Inject constructor(
    private val serviceDAO: ServiceDAO,
    private val boardingHouseDAO: BoardingHouseDAO,
    private val roomDAO: RoomDAO,
) {

    suspend fun getBoardingServiceByUserId(boardingHouseId: String): List<Service> {
        val serviceEntities = serviceDAO.getServiceByKhuTroId(boardingHouseId)
        return serviceEntities.map { it.toModel() }
    }

    suspend fun createService(service: Service): Resource<Service> {
        return try {
            val serviceEntity = service.toCreateEntity()
            serviceDAO.insert(serviceEntity)
            Resource.Success(serviceEntity.toModel(), message = "Thêm dịch vụ thành công")
        }catch (e: Exception){
            Log.e("TAG", "updateService: ${e.toString()}", )
            Resource.Error(message = e.toString())
        }
    }

    suspend fun updateService(service: Service): Resource<Service> {
        return try {
            val serviceEntity = service.toEntity()
            serviceDAO.update(serviceEntity)
            Resource.Success(serviceEntity.toModel(), message = "Sửa dịch vụ thành công")
        }catch (e: Exception){
            Log.e("TAG", "updateService: ${e.toString()}", )
            Resource.Error(message = e.toString())
        }
    }

    suspend fun deleteService(service: Service): Resource<Service> {
        return try {
            val serviceEntity = service.toEntity()
            serviceDAO.delete(serviceEntity)
            Resource.Success(serviceEntity.toModel(), message = "Xóa dịch vụ thành công")
        }catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }

    suspend fun updateRoomService(
        service: Service,
        boardingHouseId: String,
        isUpdate: Boolean = true
    ): Resource<List<Room>> {
        val roomEntities = roomDAO.getPhongsByKhuTroId(boardingHouseId)
        return try {
            return Resource.Success(
                roomEntities.map {
                    it.toModel().apply {
                        if(isUpdate){
                            if(services?.contains(service.name) != true){
                                if(services == null){
                                    services = service.name
                                }else{
                                    services += ",${service.name}"
                                }
                                roomDAO.update(toEntity())
                                Log.e("TAG", "updateServiceRoom update: ${services}")
                            }
                            // remove service to room
                        }else{
                            Log.e("TAG", "updateServiceRoom delete: ${services}")
                            if(services?.contains(service.name) == true){
                                services = services?.replace(",${service.name}", "")?.replace(service.name, "")
                                roomDAO.update(toEntity())
                            }
                        }
                    }
                }
            )
        }   catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }

    suspend fun getServiceByRoom(boardingHouse: String, roomId: String): Resource<List<Service>> {
        return try {
            val services = serviceDAO.getServiceOfRoom(boardingHouse, roomId)
            Resource.Success(services.map { it.toModel() })
        }catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }
}