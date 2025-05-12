package com.app.motel.feature.revenue.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.entity.HoaDonEntity
import com.app.motel.data.model.Resource
import com.app.motel.data.repository.BillRepository
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

class RevenueViewModel @Inject constructor(
    private val billRepository: BillRepository,
    private val userController: UserController
) : AppBaseViewModel<RevenueViewState, RevenueViewAction, RevenueViewEvent>(RevenueViewState()) {

    var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    var currentYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun handle(action: RevenueViewAction) {
        // Handle actions if needed
    }

    fun getBills() {
        liveData.bills.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val bills = if (userController.state.isAdmin) {
                    val boardingHouseId = userController.state.currentBoardingHouseId
                    billRepository.getBillByBoardingHouseId(boardingHouseId)
                } else {
                    val currentUserId = userController.state.currentUserId
                    billRepository.getBillByTenantRentedRoom(currentUserId)
                }

                // Filter by the current month and year and paid status
                val filteredBills = bills.filter {
                    it.status == HoaDonEntity.STATUS_PAID &&
                            it.month == currentMonth &&
                            it.year == currentYear
                }

                liveData.bills.postValue(Resource.Success(filteredBills))
            } catch (e: Exception) {
                liveData.bills.postValue(Resource.Error(message = e.toString()))
            }
        }
    }

    fun getPaidBillsByMonth(month: Int) {
        currentMonth = month
        getBills()
    }
}