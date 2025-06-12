package com.app.motel.feature.handleContract.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.app.motel.common.service.DateConverter
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.entity.HoaDonEntity
import com.app.motel.data.entity.HopDongEntity
import com.app.motel.data.entity.PhongEntity
import com.app.motel.data.model.Contract
import com.app.motel.data.model.Resource
import com.app.motel.data.repository.BillRepository
import com.app.motel.data.repository.ContractRepository
import com.app.motel.data.repository.TenantRepository
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class HandleContractViewModel @Inject constructor(
    val repository: ContractRepository,
    private val tenantRepository: TenantRepository,
    private val billRepository: BillRepository,
    private val userController: UserController,
): AppBaseViewModel<HandleContractViewState, HandleContractViewAction, HandleContractViewEvent>(
    HandleContractViewState()
) {
    init {
        liveData.isAdmin.value = userController.state.isAdmin
    }

    override fun handle(action: HandleContractViewAction) {
    }

    fun setCurrentStateContract(position: Int){
        val state = when(position){
            0 -> Contract.State.ACTIVE
            1 -> Contract.State.NEAR_END
            2 -> Contract.State.ENDED
            else -> Contract.State.ENDED
        }
        liveData.currentStateContract.postValue(state)
    }

    fun getContracts(){
        liveData.contracts.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val contracts = liveData.isAdmin.value.let {
                    if(it == true){
                        // Admin: Lấy hợp đồng theo nhà trọ hiện tại
                        val boardingHouseId = userController.state.currentBoardingHouseId
                        repository.getContractByBoardingHouseId(boardingHouseId)
                    } else{
                        // Người thuê: Lấy hợp đồng của họ
                        val tenantId = userController.state.currentUserId
                        repository.getContractByTenantId(tenantId)
                    }
                }
                liveData.contracts.postValue(Resource.Success(contracts))
            }catch (e: Exception){
                Log.e("HandleContractViewModel", e.toString())
                liveData.contracts.postValue(Resource.Error(message = e.toString()))
            }
        }
    }

    fun updateContract(contract: Contract) {
        viewModelScope.launch {
            liveData.updateContract.postValue(Resource.Loading())

            // Tính lại thời hạn hợp đồng trước khi cập nhật
            val updatedContract = calculateDuration(contract)

            val result = repository.updateContract(updatedContract)
            if (result.isSuccess()) {
                // Refresh the contract list if update was successful
                getContracts()
            }
            liveData.updateContract.postValue(result)
        }
    }

    /**
     * Tính toán thời hạn hợp đồng dựa trên ngày bắt đầu và ngày kết thúc
     */
    private fun calculateDuration(contract: Contract): Contract {
        try {
            val startDateStr = contract.startDate
            val endDateStr = contract.endDate

            if (!startDateStr.isNullOrEmpty() && !endDateStr.isNullOrEmpty()) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                val startDate = dateFormat.parse(startDateStr)
                val endDate = dateFormat.parse(endDateStr)

                if (startDate != null && endDate != null) {
                    val startCalendar = Calendar.getInstance()
                    startCalendar.time = startDate

                    val endCalendar = Calendar.getInstance()
                    endCalendar.time = endDate

                    // Tính toán số tháng giữa hai ngày
                    val yearDiff = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)
                    val monthDiff = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH)

                    // Công thức tính thời hạn theo tháng
                    var months = yearDiff * 12 + monthDiff

                    // Chỉ cộng thêm 1 tháng khi ngày kết thúc lớn hơn ngày bắt đầu
                    // Nếu ngày kết thúc = ngày bắt đầu, đó chính xác là N tháng (không cộng thêm)
                    if (endCalendar.get(Calendar.DAY_OF_MONTH) > startCalendar.get(Calendar.DAY_OF_MONTH)) {
                        months += 1
                    }

                    // Đảm bảo thời hạn không âm
                    return contract.copy(duration = months.coerceAtLeast(0))
                }
            }
            return contract
        } catch (e: Exception) {
            Log.e("HandleContractViewModel", "Error calculating duration: ${e.message}")
            return contract
        }
    }

    fun refreshContract(
        contract: Contract,
        duration: Int?,
        newEndDate: String?,
    ){
        liveData.updateContract.postValue(Resource.Loading())
        val currentUser = userController.state.currentUser.value?.data
        when {
            currentUser == null || !currentUser.isAdmin -> {
                liveData.updateContract.postValue(Resource.Error(message = "Bạn không có quyền tạo"))
                return
            }
            duration == null -> {
                liveData.updateContract.postValue(Resource.Error(message = "Thời hạn hợp đồng mới là bắt buộc"))
                return
            }
            duration <= 0 -> {
                liveData.updateContract.postValue(Resource.Error(message = "Thời hạn hợp đồng mới không hợp lệ"))
                return
            }
            newEndDate.isNullOrBlank() -> {
                liveData.updateContract.postValue(Resource.Error(message = "Ngày kết thúc hợp đồng mới là bắt buộc"))
                return
            }
            DateConverter.localStringToDate(newEndDate) == null -> {
                liveData.updateContract.postValue(Resource.Error(message = "Ngày kết thúc hợp đồng không hợp lệ"))
                return
            }
        }

        viewModelScope.launch {
            val contractUpdate = contract.copy(
                startDate = DateConverter.getCurrentLocalDateTime(),
                endDate = newEndDate,
                duration = duration // Thêm duration từ tham số đầu vào
            )

            val contractUpdated = repository.updateContract(contractUpdate)
            liveData.updateContract.postValue(contractUpdated)
        }
    }

    fun endContract(
        contract: Contract,
        dateEndStr: String?,
        hasResultDeposited: Boolean,
        hasFullyPaid: Boolean,
        terminationReason: String?,
        refundAmount: String?,
        deductionReason: String?
    ) {
        liveData.updateContract.postValue(Resource.Loading())

        // Validate required date
        if(DateConverter.localStringToDate(dateEndStr) == null) {
            liveData.updateContract.postValue(Resource.Error(message = "Ngày kết thúc không hợp lệ"))
            return
        }

        // Validate payment confirmations
        else if(!hasResultDeposited) {
            liveData.updateContract.postValue(Resource.Error(message = "Bạn chưa xác nhận đã trả tiền cọc"))
            return
        }
        else if(!hasFullyPaid) {
            liveData.updateContract.postValue(Resource.Error(message = "Bạn chưa xác nhận đã thanh toán đầy đủ"))
            return
        }

        // Validate termination details
        else if(terminationReason.isNullOrBlank()) {
            liveData.updateContract.postValue(Resource.Error(message = "Lý do kết thúc hợp đồng không được để trống"))
            return
        }
        else if(refundAmount.isNullOrBlank()) {
            liveData.updateContract.postValue(Resource.Error(message = "Số tiền hoàn trả không được để trống"))
            return
        }
        else if(deductionReason.isNullOrBlank()) {
            liveData.updateContract.postValue(Resource.Error(message = "Lý do khấu trừ không được để trống"))
            return
        }

        viewModelScope.launch {
            // Kiểm tra hóa đơn chưa thanh toán
            val roomId = contract.roomId
            if (roomId != null) {
                try {
                    val bills = billRepository.getBillByRoomId(roomId)
                    val unpaidBills = bills.filter { it.status == HoaDonEntity.STATUS_UNPAID }

                    if (unpaidBills.isNotEmpty()) {
                        liveData.updateContract.postValue(Resource.Error(message = "Không thể kết thúc hợp đồng vì phòng này còn ${unpaidBills.size} hóa đơn chưa thanh toán"))
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e("HandleContractViewModel", "Error checking unpaid bills: ${e.message}")
                    liveData.updateContract.postValue(Resource.Error(message = "Lỗi khi kiểm tra hóa đơn: ${e.message}"))
                    return@launch
                }
            }

            // Tính lại thời hạn hợp đồng dựa trên ngày kết thúc mới
            val contractWithEndDate = contract.copy(endDate = dateEndStr)
            val updatedContract = calculateDuration(contractWithEndDate).copy(
                endDate = dateEndStr,
                isActive = HopDongEntity.INACTIVE,
                deposit = "${contract.deposit} (Đã trả)",
                // Thêm các thông tin kết thúc hợp đồng
                terminationReason = terminationReason,
                refundAmount = refundAmount,
                deductionReason = deductionReason
            )

            val contractUpdated = repository.updateContract(updatedContract)
            if(contractUpdated.isSuccess()) {
                // Cập nhật trạng thái phòng thành trống
                repository.updateStateRoom(contractUpdated.data?.roomId ?: "", PhongEntity.Status.EMPTY.value)

                // Xóa người thuê khỏi phòng
                tenantRepository.removeTenantFromRoom(contractUpdated.data?.roomId ?: "")

                // Đặt lại trạng thái người đại diện hợp đồng
                contract.customerId?.let { tenantId ->
                    try {
                        // Câp nhật trạng thái người đại diện hợp đồng về false
                        tenantRepository.updateTenantContractHolderStatus(tenantId, false)
                        Log.d("HandleContractViewModel", "Reset contract holder status for tenant ID: $tenantId")
                    } catch (e: Exception) {
                        Log.e("HandleContractViewModel", "Error updating tenant contract holder status: ${e.message}")
                    }
                }
            }
            liveData.updateContract.postValue(contractUpdated.apply {
                message = "Kết thúc hợp đồng thành công"
            })
        }
    }
}