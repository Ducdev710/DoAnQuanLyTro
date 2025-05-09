package com.app.motel.feature.home.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.model.Resource
import com.app.motel.data.repository.BillRepository
import com.app.motel.data.repository.ContractRepository
import com.app.motel.data.repository.HomeRepository
import com.app.motel.data.repository.RoomRepository
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val repo: HomeRepository,
    private val roomRepository: RoomRepository,
    private val contractRepository: ContractRepository,
    private val billRepository: BillRepository,
    val userController: UserController,
    ) : AppBaseViewModel<HomeViewLiveData, HomeViewAction, HomeViewEvent>(HomeViewLiveData()) {

    override fun handle(action: HomeViewAction) {

    }

    fun getBoardingById(boardingHouseId: String?) {
        liveData.boardingHouse.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val user = userController.state.currentUser.value?.data
                when{
                    user?.id.isNullOrBlank() -> {
                        liveData.boardingHouse.postValue(Resource.Error(message = "Không tìm thấy người dùng"))
                        return@launch
                    }
                    user?.isAdmin != true -> {
                        liveData.boardingHouse.postValue(Resource.Error(message = "Người dùng không phải là quản lý"))
                        return@launch
                    }
                    boardingHouseId.isNullOrBlank() -> {
                        liveData.boardingHouse.postValue(Resource.Error(message = "Không tìm thấy nhà trọ"))
                        return@launch
                    }
                }

                val response = roomRepository.getBoardingRoomById(boardingHouseId ?: "")
                liveData.boardingHouse.postValue(Resource.Success(response))
            } catch (e: Exception) {
                liveData.boardingHouse.postValue(Resource.Error(message = e.toString()))
            }
        }
    }

    fun getContracts(){
        liveData.contracts.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val boardingHouseId = userController.state.currentBoardingHouseId
                val contracts = contractRepository.getContractByBoardingHouseId(boardingHouseId)
                liveData.contracts.postValue(Resource.Success(contracts))
            }catch (e: Exception){
                liveData.contracts.postValue(Resource.Error(message = e.toString()))
            }
        }
    }

    fun getBills(){
        liveData.bills.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val boardingHouseId = userController.state.currentBoardingHouseId
                val bills = billRepository.getBillByBoardingHouseId(boardingHouseId)
                liveData.bills.postValue(Resource.Success(bills))
            }catch (e: Exception){
                liveData.bills.postValue(Resource.Error(message = e.toString()))
            }
        }
    }

    fun logout(){
        userController.logout()
        liveData.boardingHouse.postValue(Resource.Initialize())
    }
}