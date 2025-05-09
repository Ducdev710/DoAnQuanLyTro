package com.app.motel.feature.complaint.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.model.Complaint
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.repository.ComplaintRepository
import com.app.motel.data.repository.ContractRepository
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import javax.inject.Inject

class ComplaintViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository,
    private val contractRepository: ContractRepository,
    private val userController: UserController
): AppBaseViewModel<ComplaintState, ComplaintViewAction, ComplaintViewEvent>(ComplaintState()) {
    override fun handle(action: ComplaintViewAction) {

    }

    fun getComplaint(){
        viewModelScope.launch {
            val complaints = complaintRepository.getComplainByUser(userController.state.currentUserId)
            Log.e("ComplaintViewModel", "getComplaint: ${complaints}")
            liveData.complains.postValue(complaints)
        }
    }

    fun initForm(complaint: Complaint?){
        liveData.currentComplain.postValue(complaint)
    }

    fun clearForm() {
        liveData.currentComplain.postValue(null)
        liveData.updateComplaint.postValue(Resource.Initialize())
    }

    fun addComplaint(title: String, content: String){
        viewModelScope.launch {

            liveData.updateComplaint.postValue(Resource.Loading())
            val currentUserId = userController.state.currentUserId
            val currentContract = contractRepository.getContractActiveByTenantId(currentUserId)
            when {
                currentUserId.isBlank() -> {
                    liveData.updateComplaint.postValue(Resource.Error(message = "Không tìm thấy người dùng"))
                    return@launch
                }
                currentContract == null -> {
                    liveData.updateComplaint.postValue(Resource.Error(message = "Không tìm thấy phòng hiện tại của bạn"))
                    return@launch
                }
                title.isBlank() -> {
                    liveData.updateComplaint.postValue(Resource.Error(message = "Tiêu đề không được để trống"))
                    return@launch
                }
                content.isBlank() -> {
                    liveData.updateComplaint.postValue(Resource.Error(message = "Nội dung không được để trống"))
                    return@launch
                }
            }

            val rentRoomInsert = Complaint(
                title = title,
                content = content,
                submittedBy = currentUserId,
                roomId = currentContract?.roomId,
            )

            val rentRoom = complaintRepository.createComplaint(rentRoomInsert)
            liveData.updateComplaint.postValue(rentRoom)
        }
    }
}