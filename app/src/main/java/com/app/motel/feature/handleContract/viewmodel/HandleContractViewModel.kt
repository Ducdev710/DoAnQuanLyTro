package com.app.motel.feature.handleContract.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.app.motel.common.service.DateConverter
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.entity.HopDongEntity
import com.app.motel.data.entity.NguoiThueEntity
import com.app.motel.data.entity.PhongEntity
import com.app.motel.data.model.Contract
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Tenant
import com.app.motel.data.repository.ContractRepository
import com.app.motel.data.repository.TenantRepository
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import javax.inject.Inject

class HandleContractViewModel @Inject constructor(
    val repository: ContractRepository,
    private val tenantRepository: TenantRepository,
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
                        val boardingHouseId = userController.state.currentBoardingHouseId
                        repository.getContractByBoardingHouseId(boardingHouseId)
                    } else{
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
            val result = repository.updateContract(contract)
            if (result.isSuccess()) {
                // Refresh the contract list if update was successful
                getContracts()
            }
            liveData.updateContract.postValue(result)
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
                endDate = newEndDate
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
            val contractUpdate = contract.copy(
                endDate = dateEndStr,
                isActive = HopDongEntity.INACTIVE,
                deposit = "${contract.deposit} (Đã trả)",
                // Add termination details
                terminationReason = terminationReason,
                refundAmount = refundAmount,
                deductionReason = deductionReason
            )

            val contractUpdated = repository.updateContract(contractUpdate)
            if(contractUpdated.isSuccess()) {
                repository.updateStateRoom(contractUpdated.data?.roomId ?: "", PhongEntity.Status.EMPTY.value)
                tenantRepository.removeTenantFromRoom(contractUpdated.data?.roomId ?: "")
            }
            liveData.updateContract.postValue(contractUpdated.apply {
                message = "Kết thúc hợp đồng thành công"
            })
        }
    }
}