package com.app.motel.feature.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.model.Resource
import com.app.motel.data.repository.*
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val repo: HomeRepository,
    private val roomRepository: RoomRepository,
    private val contractRepository: ContractRepository,
    private val billRepository: BillRepository,
    private val complaintRepository: ComplaintRepository,
    private val notificationRepository: NotificationRepository,
    val userController: UserController,
) : AppBaseViewModel<HomeViewLiveData, HomeViewAction, HomeViewEvent>(HomeViewLiveData()) {

    private val _notificationCount = MutableLiveData<Int>(0)
    val notificationCount: LiveData<Int> = _notificationCount

    override fun handle(action: HomeViewAction) {
        // Implementation
    }

    init {
        refreshNotificationCount()
    }

    fun refreshNotificationCount() {
        viewModelScope.launch {
            try {
                val user = userController.state.getCurrentUser
                val count = if (user?.isAdmin == true) {
                    // For admin, get complaint notifications
                    val boardingHouseId = userController.state.currentBoardingHouseId
                    complaintRepository.getNewNotificationsForAdmin(boardingHouseId)
                } else {
                    // For tenant, get unread notifications
                    val tenantId = userController.state.currentUserId
                    notificationRepository.countUnreadNotificationsForTenant(tenantId)
                }
                _notificationCount.postValue(count)
                Log.d("HomeViewModel", "${if (user?.isAdmin == true) "Admin" else "Tenant"} notifications count: $count")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error getting notification count: ${e.message}", e)
                _notificationCount.postValue(0)
            }
        }
    }

    fun getNewNotificationsCount(): LiveData<Int> = notificationCount

    fun getBoardingById(boardingHouseId: String?) {
        liveData.boardingHouse.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val user = userController.state.currentUser.value?.data
                when {
                    user?.id.isNullOrBlank() -> {
                        liveData.boardingHouse.postValue(Resource.Error(message = "Không tìm thấy người dùng"))
                        return@launch
                    }
                    //Kiểm tra quyền truy cập (phải là admin)
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

    fun getContracts() {
        liveData.contracts.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val boardingHouseId = userController.state.currentBoardingHouseId
                val contracts = contractRepository.getContractByBoardingHouseId(boardingHouseId)
                liveData.contracts.postValue(Resource.Success(contracts))
            } catch (e: Exception) {
                liveData.contracts.postValue(Resource.Error(message = e.toString()))
            }
        }
    }

    fun getBills() {
        liveData.bills.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val boardingHouseId = userController.state.currentBoardingHouseId
                val bills = billRepository.getBillByBoardingHouseId(boardingHouseId)
                liveData.bills.postValue(Resource.Success(bills))
            } catch (e: Exception) {
                liveData.bills.postValue(Resource.Error(message = e.toString()))
            }
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            try {
                val user = userController.state.getCurrentUser
                if (user?.isAdmin != true) {
                    // Only mark notifications as read for tenant users
                    val tenantId = userController.state.currentUserId
                    val count = notificationRepository.markAllNotificationsAsReadForTenant(tenantId)
                    Log.d("HomeViewModel", "Marked $count notifications as read for tenant $tenantId")
                    // Refresh notification count after marking as read
                    refreshNotificationCount()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error marking notifications as read: ${e.message}", e)
            }
        }
    }

    fun logout() {
        userController.logout()
        liveData.boardingHouse.postValue(Resource.Initialize())
    }
}