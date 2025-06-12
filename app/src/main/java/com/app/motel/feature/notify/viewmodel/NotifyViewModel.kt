package com.app.motel.feature.notify.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.entity.KhieuNaiEntity
import com.app.motel.data.model.Complaint
import com.app.motel.data.model.Resource
import com.app.motel.data.repository.ComplaintRepository
import com.app.motel.data.repository.NotificationRepository
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotifyViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository,
    private val notificationRepository: NotificationRepository,
    val userController: UserController,
): AppBaseViewModel<NotifyViewState, NotifyViewAction, NotifyViewEvent>(NotifyViewState()) {
    override fun handle(action: NotifyViewAction) {

    }

    //Thiết lập tab hiển thị
    fun setCurrentType(position: Int){
        //Chủ trọ
        if(liveData.isAdmin){
            when(position){
                0 -> liveData.currentTabType.postValue(KhieuNaiEntity.Type.APPLICATION)
                1 -> liveData.currentTabType.postValue(KhieuNaiEntity.Type.COMPLAINT)
                2 -> liveData.currentTabType.postValue(KhieuNaiEntity.Type.RENT_ROOM)
                else -> liveData.currentTabType.postValue(KhieuNaiEntity.Type.APPLICATION)
            }
            return
        }
        //Người thuê
        when(position){
            0 -> liveData.currentTabGeneral.postValue(true)
            1 -> liveData.currentTabGeneral.postValue(false)
            else -> liveData.currentTabGeneral.postValue(true)
        }
    }

    //Lấy danh sách khiếu nại của chủ nhà trọ
    fun getNotificationAdmin(){
        viewModelScope.launch {
            try {
                val complaints = complaintRepository.getComplaintAdmin(userController.state.currentBoardingHouseId)
                liveData.complaints.postValue(complaints)
            }catch (e: Exception){
                Log.e("NotifyViewModel", "lỗi: complaints: ${e.message}")
            }
        }
    }

    //Lấy danh sách thông báo của người thuê
    fun getNotificationUser(){
        viewModelScope.launch {
            try {
                val notifications = notificationRepository.getNotificationByTenantId(userController.state.currentUserId)
                Log.e("NotifyViewModel", "complaints: $notifications")
                liveData.notifications.postValue(notifications)
            }catch (e: Exception){
                Log.e("NotifyViewModel", "lỗi: complaints: ${e.message}")
            }
        }
    }

    //Lưu khiếu nại đang được xử lý
    fun setCurrentHandleComplaint(item: Complaint?) {
        liveData.currentHandleComplaint.postValue(item)
    }

    fun updateStateComplaint(complaint: Complaint, state: String){
        // Nếu là thông báo ứng dụng (APPLICATION), cho phép cập nhật trạng thái
        if (complaint.type == KhieuNaiEntity.Type.APPLICATION.value) {
            viewModelScope.launch {
                try {
                    complaintRepository.updateStateComplaint(complaint.id, state)
                    getNotificationAdmin()
                } catch (e: Exception) {
                    Log.e("NotifyViewModel", "Lỗi cập nhật trạng thái thông báo: ${e.message}")
                    liveData.updateComplaint.postValue(Resource.Error(message = "Lỗi cập nhật trạng thái: ${e.message}"))
                }
            }
            return
        }

        // Xử lý các loại khiếu nại khác như trước
        when{
            complaint.isSystemNotification -> {
                liveData.updateComplaint.postValue(Resource.Error(message = "Không thể cập nhật trạng thái thông báo hệ thống"))
                return
            }
            complaint.id.isBlank() -> {
                liveData.updateComplaint.postValue(Resource.Error(message = "Không tìm thấy khiếu nại yêu cầu"))
                return
            }
            state.isBlank() -> {
                liveData.updateComplaint.postValue(Resource.Error(message = "Trạng thái không được để trống"))
                return
            }
            state !in KhieuNaiEntity.Status.entries.map { it.value } -> {
                liveData.updateComplaint.postValue(Resource.Error(message = "Trạng thái không hợp lệ"))
                return
            }
            state == complaint.status -> {
                liveData.updateComplaint.postValue(Resource.Error(message = "Trạng thái không thay đổi"))
                return
            }
            (complaint.status == KhieuNaiEntity.Status.RESOLVED.value
                    || complaint.status == KhieuNaiEntity.Status.REJECTED.value
                    ) && (state == KhieuNaiEntity.Status.NEW.value
                    || state == KhieuNaiEntity.Status.PENDING.value
                    || state == KhieuNaiEntity.Status.RESOLVED.value
                    || state == KhieuNaiEntity.Status.REJECTED.value) -> {
                liveData.updateComplaint.postValue(Resource.Error(message = " Khiếu nại đã được xử lý rồi"))
                return
            }
        }
        viewModelScope.launch {
            complaintRepository.updateStateComplaint(complaint.id, state)
            getNotificationAdmin()
        }
    }

    // Thêm phương thức mới để cập nhật tất cả thông báo ứng dụng
    fun updateAllApplicationNotifications() {
        viewModelScope.launch {
            try {
                // Gọi repository để cập nhật tất cả thông báo APPLICATION từ "Mới" thành "Đã xử lý"
                val count = complaintRepository.updateAllApplicationNotifications(
                    userController.state.currentBoardingHouseId,
                    KhieuNaiEntity.Status.RESOLVED.value
                )
                if (count > 0) {
                    // Nếu có thông báo được cập nhật, refresh lại danh sách
                    getNotificationAdmin()
                    Log.d("NotifyViewModel", "Đã cập nhật $count thông báo ứng dụng")
                }
            } catch (e: Exception) {
                Log.e("NotifyViewModel", "Lỗi cập nhật tất cả thông báo ứng dụng: ${e.message}")
            }
        }
    }
}