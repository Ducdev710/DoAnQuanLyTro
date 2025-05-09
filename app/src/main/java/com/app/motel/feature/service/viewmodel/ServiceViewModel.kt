package com.app.motel.feature.service.viewmodel

import androidx.lifecycle.viewModelScope
import com.app.motel.common.ultis.toStringMoney
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Service
import com.app.motel.data.repository.ServiceRepository
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import javax.inject.Inject

class ServiceViewModel @Inject constructor(
    private val repository: ServiceRepository,
    private val userController: UserController,
): AppBaseViewModel<ServiceViewState, ServiceViewAction, ServiceViewEvent>(
    ServiceViewState()
) {
    override fun handle(action: ServiceViewAction) {
    }

    fun initForm(item: Service?) {
        liveData.currentService.postValue(item)
    }

    fun clearForm(){
        liveData.currentService.postValue(null)
        liveData.createService.postValue(Resource.Initialize())
    }

    fun getService(){
        liveData.services.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val services = repository.getBoardingServiceByUserId(userController.state.currentBoardingHouseId)
                liveData.services.postValue(Resource.Success(services))
            }catch (e: Exception){
                liveData.services.postValue(Resource.Error(message = e.toString()))
            }
        }
    }

    fun createService(
        name: String?,
        price: String?,
        typePay: String?,
        isAppliesAllRoom: Boolean,
    ){
        liveData.createService.postValue(Resource.Loading())
        val currentUser = userController.state.currentUser.value?.data
        when {
            currentUser == null || !currentUser.isAdmin -> {
                liveData.createService.postValue(Resource.Error(message = "Bạn không có quyền tạo"))
                return
            }
            userController.state.currentBoardingHouse.value == null -> {
                liveData.createService.postValue(Resource.Error(message = "Không tìm thấy khu trọ của bạn"))
                return
            }
            name.isNullOrBlank() -> {
                liveData.createService.postValue(Resource.Error(message = "Tên dịch vụ là bắt buộc"))
                return
            }
            typePay.isNullOrBlank() -> {
                liveData.createService.postValue(Resource.Error(message = "Giá dịch vụ là bắt buộc"))
                return
            }
        }

        viewModelScope.launch {
            val isCreate = liveData.currentService.value == null
            val newService = if(isCreate) Service(
                name = name!!,
                price = price.toStringMoney(),
                typePay = typePay,
                areaId = userController.state.currentBoardingHouseId,
                roomId = if (isAppliesAllRoom) null else liveData.currentService.value?.roomId,
            ) else liveData.currentService.value!!.copy(
                name = name!!,
                price = price.toStringMoney(),
                typePay = typePay,
                roomId = if (isAppliesAllRoom) null else liveData.currentService.value?.roomId,
            )

            val serviceCreated = if(isCreate) repository.createService(newService)
                else repository.updateService(newService)
            if(serviceCreated.isSuccess()){
                if(serviceCreated.data?.isAppliesAllRoom == true){
                    repository.updateRoomService(serviceCreated.data, userController.state.currentBoardingHouseId)
                }
            }
            liveData.createService.postValue(serviceCreated)
        }
    }

    fun deleteService(serviceDelete: Service?){
        liveData.createService.postValue(Resource.Loading())
        val currentUser = userController.state.currentUser.value?.data
        when {
            currentUser == null || !currentUser.isAdmin -> {
                liveData.createService.postValue(Resource.Error(message = "Bạn không có quyền tạo"))
                return
            }
            userController.state.currentBoardingHouse.value == null -> {
                liveData.createService.postValue(Resource.Error(message = "Không tìm thấy khu trọ của bạn"))
                return
            }
            serviceDelete == null -> {
                liveData.createService.postValue(Resource.Error(message = "Không tìm thấy dịch vụ"))
                return
            }
        }
        viewModelScope.launch {
            val serviceDeleted = repository.deleteService(serviceDelete!!)

            if(serviceDeleted.isSuccess()){
                repository.updateRoomService(
                    service = serviceDeleted.data!!,
                    boardingHouseId = userController.state.currentBoardingHouseId,
                    isUpdate = false
                )
            }
            liveData.createService.postValue(serviceDeleted)
        }
    }
}