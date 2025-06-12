package com.app.motel.feature.notify.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.entity.KhieuNaiEntity
import com.app.motel.data.model.Complaint
import com.app.motel.data.model.Notification
import com.app.motel.data.model.Resource

class NotifyViewState: AppViewLiveData {
    //Xác định người dùng là chủ hay người thuê để hiển thị UI và các tab hiển thị
    var isAdmin = false

    //Danh sách tất cả khiếu nại(Phia chủ nhà trọ)
    //Loại khiếu nại đang được hiển thị (mặc định là APPLICATION)
    val complaints = MutableLiveData<List<Complaint>>()
    val currentTabType = MutableLiveData(KhieuNaiEntity.Type.APPLICATION)

    //Danh sách tất cả thông báo(Phía người thuê)
    //Xác định tab đang hiển thị (true: thông báo chung, false: thông báo phòng)
    val notifications = MutableLiveData<List<Notification>>()
    val currentTabGeneral = MutableLiveData(true)

    //Xử lý khiếu nại
    val currentHandleComplaint = MutableLiveData<Complaint>()
    val updateComplaint = MutableLiveData<Resource<Complaint>>()

    //Lọc danh sách khiếu nại theo loại (APPLICATION, COMPLAINT, RENT_ROOM)(phía chủ nhà trọ):
    //Đảo ngược thứ tự để hiển thị mới nhất lên đầu
    //Trả về danh sách rỗng nếu không có dữ liệu
    val getNotifyAdmin: List<Complaint> get () = complaints.value?.filter {
        it.type == currentTabType.value?.value
    }?.reversed() ?: arrayListOf()

    //Lọc thông báo dựa trên tab hiện tại(phia người thuê):
    //Tab chung: Chỉ hiển thị thông báo không gắn với phòng cụ thể (phongId=null)
    //Tab phòng: Chỉ hiển thị thông báo gắn với phòng (phongId!=null)
    //Đảo ngược thứ tự để hiển thị mới nhất lên đầu
    //Trả về danh sách rỗng nếu không có dữ liệu
    val getNotifyUser: List<Notification> get () = notifications.value?.filter {
        currentTabGeneral.value == true && it.phongId == null
        || currentTabGeneral.value == false && it.phongId != null
    }?.reversed() ?: arrayListOf()
}