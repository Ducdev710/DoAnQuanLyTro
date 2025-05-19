package com.app.motel.feature.handleBill.viewmodel

import androidx.lifecycle.viewModelScope
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.entity.HoaDonEntity
import com.app.motel.data.model.Bill
import com.app.motel.data.model.Contract
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Status
import com.app.motel.data.model.Tenant
import com.app.motel.data.repository.BillRepository
import com.app.motel.data.repository.ContractRepository
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
        // Handle actions from UI
    }

    fun getBills() {
        liveData.bills.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val bills = userController.state.isAdmin.let {
                    if (it) {
                        val boardingHouseId = userController.state.currentBoardingHouseId
                        billRepository.getBillByBoardingHouseId(boardingHouseId)
                    } else {
                        val currentUserId = userController.state.currentUserId
                        billRepository.getBillByTenantRentedRoom(currentUserId)
                    }
                }
                liveData.bills.postValue(Resource.Success(bills))
            } catch (e: Exception) {
                liveData.bills.postValue(Resource.Error(message = e.toString()))
            }
        }
    }

    fun initForm(bill: Bill) {
        viewModelScope.launch {
            try {
                // Get tenant info
                val contract: Contract? = contractRepository.getContractActiveByRoomId(bill.roomId ?: "")
                val tenant: Tenant? = tenantRepository.getTenantsById(contract?.customerId ?: "")
                bill.tenant = tenant

                // Set current bill directly (no need for previous bill now)
                liveData.currentBill.postValue(bill)
            } catch (e: Exception) {
                liveData.updateBill.postValue(Resource.Error(message = "Lỗi khởi tạo: ${e.message}"))
            }
        }
    }

    fun clearForm() {
        liveData.currentBill.postValue(null)
        liveData.updateBill.postValue(Resource.Initialize())
        getBills()
    }

    fun payingBill(bill: Bill?) {
        viewModelScope.launch {
            try {
                liveData.updateBill.postValue(Resource.Loading())
                val currentUser = userController.state.getCurrentUser

                when {
                    bill == null -> {
                        liveData.updateBill.postValue(Resource.Error(message = "Hóa đơn không tồn tại"))
                        return@launch
                    }
                    bill.status == HoaDonEntity.STATUS_PAID -> {
                        liveData.updateBill.postValue(Resource.Error(message = "Hóa đơn đã được thanh toán"))
                        return@launch
                    }
                    // Check if current user is associated with this bill through any contracts
                    currentUser == null -> {
                        liveData.updateBill.postValue(Resource.Error(message = "Không thể xác thực người dùng"))
                        return@launch
                    }
                    !userController.state.isAdmin && !isUserAllowedToPayBill(currentUser.id, bill) -> {
                        liveData.updateBill.postValue(Resource.Error(message = "Bạn không có quyền thanh toán hóa đơn này"))
                        return@launch
                    }
                }

                val billUpdate = bill!!.copy(
                    status = HoaDonEntity.STATUS_PAID
                )
                val billUpdated = billRepository.updateBill(billUpdate)

                if (billUpdated.status == Status.SUCCESS) {
                    liveData.currentBill.postValue(billUpdate)
                }

                liveData.updateBill.postValue(billUpdated)
            } catch (e: Exception) {
                liveData.updateBill.postValue(Resource.Error(message = "Lỗi thanh toán: ${e.message}"))
            }
        }
    }

    /**
     * Helper method to check if a user is allowed to pay a bill
     * This allows tenants to pay bills even when contracts are nearly expired
     */
    private suspend fun isUserAllowedToPayBill(userId: String, bill: Bill): Boolean {
        // Get all contracts associated with this user
        val userContracts = contractRepository.getContractByTenantId(userId)

        // Check if any of the user's contracts are for the room in this bill
        return userContracts.any { contract ->
            contract.roomId == bill.roomId
        }
    }

    /**
     * Updates an existing bill with new values and meter readings
     */
    fun updateBill(bill: Bill?) {
        viewModelScope.launch {
            try {
                liveData.updateBill.postValue(Resource.Loading())

                // Basic validations
                when {
                    bill == null -> {
                        liveData.updateBill.postValue(Resource.Error(message = "Hóa đơn không tồn tại"))
                        return@launch
                    }
                    !userController.state.isAdmin -> {
                        liveData.updateBill.postValue(Resource.Error(message = "Bạn không có quyền chỉnh sửa hóa đơn"))
                        return@launch
                    }
                    bill.status == HoaDonEntity.STATUS_PAID -> {
                        liveData.updateBill.postValue(Resource.Error(message = "Không thể chỉnh sửa hóa đơn đã thanh toán"))
                        return@launch
                    }
                    bill.electricityIndex == null -> {
                        liveData.updateBill.postValue(Resource.Error(message = "Chỉ số điện không được để trống"))
                        return@launch
                    }
                    bill.waterIndex == null -> {
                        liveData.updateBill.postValue(Resource.Error(message = "Chỉ số nước không được để trống"))
                        return@launch
                    }
                }

                val billNonNull = bill!!
                val electricityIndex = billNonNull.electricityIndex!!
                val waterIndex = billNonNull.waterIndex!!

                // Use previous index values from the bill itself
                val oldElectricityIndex = billNonNull.previousElectricityIndex ?: 0
                val oldWaterIndex = billNonNull.previousWaterIndex ?: 0

                // Verify readings are increasing
                if (electricityIndex < oldElectricityIndex) {
                    liveData.updateBill.postValue(Resource.Error(message =
                    "Chỉ số điện mới ($electricityIndex) không thể nhỏ hơn chỉ số cũ ($oldElectricityIndex)"))
                    return@launch
                }

                if (waterIndex < oldWaterIndex) {
                    liveData.updateBill.postValue(Resource.Error(message =
                    "Chỉ số nước mới ($waterIndex) không thể nhỏ hơn chỉ số cũ ($oldWaterIndex)"))
                    return@launch
                }

                // Calculate consumption and costs
                val electricityUsed = electricityIndex - oldElectricityIndex
                val waterUsed = waterIndex - oldWaterIndex

                val roomPrice = billNonNull.roomPrice ?: 0.0
                val electricityCost = electricityUsed * HoaDonEntity.PRICE_ELECTRICITY
                val waterCost = waterUsed * HoaDonEntity.PRICE_WATER

                // Parse service fee and discount
                val serviceFee = try {
                    billNonNull.serviceFee?.replace(" VND", "")?.replace(",", "")?.replace(" ", "")?.toIntOrNull() ?: 0
                } catch (e: Exception) { 0 }

                val discount = try {
                    billNonNull.discount?.replace(" VND", "")?.replace(",", "")?.replace(" ", "")?.toIntOrNull() ?: 0
                } catch (e: Exception) { 0 }

                // Calculate total cost
                val totalInt = (roomPrice + serviceFee + electricityCost + waterCost - discount).toInt()
                val totalAmount = String.format("%,d", totalInt)

                // Create updated bill
                val updatedBill = billNonNull.copy(
                    totalAmount = totalAmount,
                    electricityIndex = electricityIndex,
                    waterIndex = waterIndex,
                    electricityUsed = electricityUsed,
                    waterUsed = waterUsed,
                    roomPrice = roomPrice,
                    serviceFee = billNonNull.serviceFee,
                    discount = billNonNull.discount,
                    note = billNonNull.note
                )

                // Submit to repository
                val result = billRepository.updateBill(updatedBill)

                if (result.status == Status.SUCCESS) {
                    liveData.currentBill.postValue(updatedBill)
                }

                liveData.updateBill.postValue(result)
            } catch (e: Exception) {
                liveData.updateBill.postValue(Resource.Error(message = "Lỗi cập nhật hóa đơn: ${e.message}"))
            }
        }
    }
}