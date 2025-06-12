package com.app.motel.feature.handleBill.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.entity.HoaDonEntity
import com.app.motel.data.model.Bill
import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.Complaint
import com.app.motel.data.model.Contract
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Status
import com.app.motel.data.model.Tenant
import com.app.motel.data.repository.BillRepository
import com.app.motel.data.repository.BoardingHouseRepository
import com.app.motel.data.repository.ComplaintRepository
import com.app.motel.data.repository.ContractRepository
import com.app.motel.data.repository.TenantRepository
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class HandleBillViewModel @Inject constructor(
    private val billRepository: BillRepository,
    private val contractRepository: ContractRepository,
    private val tenantRepository: TenantRepository,
    val userController: UserController,
    private val complaintRepository: ComplaintRepository,
    private val boardingHouseRepository: BoardingHouseRepository,
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
                        billRepository.getBillsByTenantId(currentUserId)
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
                var contract: Contract? = null

                // Try to find the contract using the ID if available
                if (bill.contractId != null && bill.contractId.isNotEmpty()) {
                    // Get all contracts and find the matching one
                    val contracts = contractRepository.getContractByBoardingHouseId(userController.state.currentBoardingHouseId)
                    contract = contracts.firstOrNull { it.id == bill.contractId }
                }

                // Fall back to roomId if no contract was found
                if (contract == null) {
                    contract = contractRepository.getContractActiveByRoomId(bill.roomId ?: "")
                }

                val tenant: Tenant? = tenantRepository.getTenantsById(contract?.customerId ?: "")
                bill.tenant = tenant

                // Set current bill directly
                liveData.currentBill.postValue(bill)
            } catch (e: Exception) {
                Log.e("HandleBillViewModel", "Error initializing bill form: ${e.message}", e)
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
                    currentUser == null -> {
                        liveData.updateBill.postValue(Resource.Error(message = "Không thể xác thực người dùng"))
                        return@launch
                    }
                    !userController.state.isAdmin && !isUserAllowedToPayBill(currentUser.id, bill) -> {
                        liveData.updateBill.postValue(Resource.Error(message = "Bạn không có quyền thanh toán hóa đơn này"))
                        return@launch
                    }
                }

                // Format current date as string for payment timestamp
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val paymentDateString = dateFormat.format(Date())

                val billUpdate = bill!!.copy(
                    status = HoaDonEntity.STATUS_PAID,
                    paymentDate = paymentDateString
                )
                val billUpdated = billRepository.updateBill(billUpdate)

                // Send notification via Complaint when a tenant pays a bill
                if (billUpdated.status == Status.SUCCESS && !userController.state.isAdmin && currentUser != null) {
                    val roomName = bill.room?.roomName ?: "không xác định"
                    val billMonth = "${bill.month}/${bill.year}"
                    val billAmount = bill.totalAmount ?: "0"

                    // Create a complaint as payment notification
                    val paymentComplaint = Complaint(
                        title = "Thông báo thanh toán hóa đơn",
                        content = "Phòng $roomName đã thanh toán tiền nhà tháng $billMonth. Số tiền: $billAmount VND",
                        submittedBy = currentUser.id,
                        roomId = bill.roomId,
                    )

                    try {
                        // Use the new dedicated method for bill payment notifications
                        val paymentNotification = complaintRepository.createBillPaymentNotification(paymentComplaint)

                        if (paymentNotification.status != Status.SUCCESS) {
                            Log.e("HandleBillViewModel", "Failed to send payment notification: ${paymentNotification.message}")
                        } else {
                            Log.d("HandleBillViewModel", "Successfully sent bill payment notification")
                        }
                    } catch (e: Exception) {
                        Log.e("HandleBillViewModel", "Error sending payment notification: ${e.message}", e)
                    }
                }

                if (billUpdated.status == Status.SUCCESS) {
                    liveData.currentBill.postValue(billUpdate)
                }

                liveData.updateBill.postValue(billUpdated)
            } catch (e: Exception) {
                Log.e("HandleBillViewModel", "Payment error: ${e.message}", e)
                liveData.updateBill.postValue(Resource.Error(message = "Lỗi thanh toán: ${e.message}"))
            }
        }
    }

    /**
     * Kiểm tra xem người dùng có quyền thanh toán hóa đơn hay không
     * Nếu hóa đơn có contractId, kiểm tra theo contractId
     */
    private suspend fun isUserAllowedToPayBill(userId: String, bill: Bill): Boolean {
        // First check by contract ID if available
        if (bill.contractId != null && bill.contractId.isNotEmpty()) {
            val userContracts = contractRepository.getContractByTenantId(userId)
            return userContracts.any { it.id == bill.contractId }
        }

        // Otherwise, check all user contracts against room ID
        val userContracts = contractRepository.getContractByTenantId(userId)
        return userContracts.any { contract ->
            contract.roomId == bill.roomId
        }
    }

    /**
     * Cập nhật hóa đơn với các chỉ số điện nước mới
     * Sử dụng chỉ số điện, nước theo cài đặt của nhà trọ hiện tại
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
                        liveData.updateBill.postValue(Resource.Error(message = "Chỉ số điện mới không được để trống"))
                        return@launch
                    }
                    bill.waterIndex == null -> {
                        liveData.updateBill.postValue(Resource.Error(message = "Chỉ số nước mới không được để trống"))
                        return@launch
                    }
                    bill.previousElectricityIndex == null -> {
                        liveData.updateBill.postValue(Resource.Error(message = "Chỉ số điện cũ không được để trống"))
                        return@launch
                    }
                    bill.previousWaterIndex == null -> {
                        liveData.updateBill.postValue(Resource.Error(message = "Chỉ số nước cũ không được để trống"))
                        return@launch
                    }
                }

                val billNonNull = bill!!
                val electricityIndex = billNonNull.electricityIndex!!
                val waterIndex = billNonNull.waterIndex!!
                val oldElectricityIndex = billNonNull.previousElectricityIndex!!
                val oldWaterIndex = billNonNull.previousWaterIndex!!

                // Verify readings are in correct order
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

                // Calculate consumption with potentially updated previous readings
                val electricityUsed = electricityIndex - oldElectricityIndex
                val waterUsed = waterIndex - oldWaterIndex

                // Get current boarding house to fetch the electricity and water prices
                val currentBoardingHouse = boardingHouseRepository.getCurrentBoardingHouse(userController.state.currentUserId)

                // Use prices from the boarding house or default if not available
                val electricityPrice = currentBoardingHouse?.giaDien ?: 3500
                val waterPrice = currentBoardingHouse?.giaNuoc ?: 20000

                val roomPrice = billNonNull.roomPrice ?: 0.0
                val electricityCost = electricityUsed * electricityPrice
                val waterCost = waterUsed * waterPrice

                // Parse service fee, additional fee, and discount
                val serviceFee = try {
                    billNonNull.serviceFee?.replace(" VND", "")?.replace(",", "")?.replace(" ", "")?.toIntOrNull() ?: 0
                } catch (e: Exception) { 0 }

                val additionalFee = try {
                    billNonNull.additionalFee?.replace(" VND", "")?.replace(",", "")?.replace(" ", "")?.toIntOrNull() ?: 0
                } catch (e: Exception) { 0 }

                val discount = try {
                    billNonNull.discount?.replace(" VND", "")?.replace(",", "")?.replace(" ", "")?.toIntOrNull() ?: 0
                } catch (e: Exception) { 0 }

                // Calculate total cost (now including additionalFee)
                val totalInt = (roomPrice + serviceFee + electricityCost + waterCost + additionalFee - discount).toInt()
                val totalAmount = String.format("%,d", totalInt)

                // Check if we need to assign a contract ID
                val contractId = billNonNull.contractId ?: run {
                    // Try to get active contract ID for this room
                    val activeContract = contractRepository.getContractActiveByRoomId(billNonNull.roomId ?: "")
                    activeContract?.id
                }

                // Create updated bill with potentially modified previous readings and contract ID
                val updatedBill = billNonNull.copy(
                    totalAmount = totalAmount,
                    electricityIndex = electricityIndex,
                    waterIndex = waterIndex,
                    previousElectricityIndex = oldElectricityIndex,
                    previousWaterIndex = oldWaterIndex,
                    electricityUsed = electricityUsed,
                    waterUsed = waterUsed,
                    roomPrice = roomPrice,
                    serviceFee = billNonNull.serviceFee,
                    additionalFee = billNonNull.additionalFee,
                    discount = billNonNull.discount,
                    note = billNonNull.note,
                    contractId = contractId
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