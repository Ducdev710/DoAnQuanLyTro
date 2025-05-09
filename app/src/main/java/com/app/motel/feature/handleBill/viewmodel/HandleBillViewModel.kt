package com.app.motel.feature.handleBill.viewmodel

import androidx.lifecycle.viewModelScope
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.entity.HoaDonEntity
import com.app.motel.data.model.Bill
import com.app.motel.data.model.Contract
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Tenant
import com.app.motel.data.repository.BillRepository
import com.app.motel.data.repository.ContractRepository
import com.app.motel.data.repository.RoomRepository
import com.app.motel.data.repository.TenantRepository
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import javax.inject.Inject

class HandleBillViewModel @Inject constructor(
    private val billRepository: BillRepository,
    private val contractRepository: ContractRepository,
    private val tenantRepository: TenantRepository,
    val userController: UserController
): AppBaseViewModel<HandleBillState, HandleBillAction, HandleBillEvent>(HandleBillState()) {
    override fun handle(action: HandleBillAction) {

    }

    fun getBills(){
        liveData.bills.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val bills = userController.state.isAdmin.let {
                    if (it){
                        val boardingHouseId = userController.state.currentBoardingHouseId
                        billRepository.getBillByBoardingHouseId(boardingHouseId)
                    }else {
                        val currentUserId = userController.state.currentUserId
                        billRepository.getBillByTenantRentedRoom(currentUserId)
                    }
                }
                liveData.bills.postValue(Resource.Success(bills))
            }catch (e: Exception){
                liveData.bills.postValue(Resource.Error(message = e.toString()))
            }
        }
    }

    fun initForm(bill: Bill){
        viewModelScope.launch {
            val contract: Contract? = contractRepository.getContractActiveByRoomId(bill.roomId ?: "")
            val tenant: Tenant? = tenantRepository.getTenantsById(contract?.customerId ?: "")
            bill.tenant = tenant
            liveData.currentBill.postValue(bill)
        }
    }

    fun clearForm(){
        liveData.currentBill.postValue(null)
        liveData.updateBill.postValue(Resource.Initialize())
        getBills()
    }

    fun payingBill(bill: Bill?){
        viewModelScope.launch {
            val currentUser = userController.state.getCurrentUser
            when{
                bill == null -> {
                    liveData.updateBill.postValue(Resource.Error(message = "Hóa đơn không tồn tại"))
                    return@launch
                }
                // only Contract Owner can pay bill
//                currentUser?.id != bill.tenant?.id -> {
                // member in room can pay bill
                !tenantRepository.getTenantsByRoomId(bill.roomId ?: "").any { it.id == currentUser?.id } -> {
                    liveData.updateBill.postValue(Resource.Error(message = "Bạn không có quyền thanh toán hóa đơn này"))
                    return@launch
                }
                bill.status == HoaDonEntity.STATUS_PAID -> {
                    liveData.updateBill.postValue(Resource.Error(message = "Hóa đơn đã được thanh toán"))
                    return@launch
                }
            }

            val billUpdate = bill!!.copy(
                status = HoaDonEntity.STATUS_PAID
            )
            val billUpdated = billRepository.updateBill(billUpdate)
            liveData.updateBill.postValue(billUpdated)

        }

    }

}