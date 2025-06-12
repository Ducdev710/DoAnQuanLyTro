package com.app.motel.feature.service.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.model.Service

class ServiceViewState: AppViewLiveData {
    // Danh sách tất cả dịch vụ của khu trọ
    val services = MutableLiveData<Resource<List<Service>>>()

    val createService = MutableLiveData<Resource<Service>>()
    val updateRoomServices = MutableLiveData<Resource<List<Room>>>()

    //Danh sách dịch vụ riêng cho một phòng cụ thể
    val roomSpecificServices = MutableLiveData<Resource<List<Service>>>()

    val currentService = MutableLiveData<Service?>()
    val selectedRoomId = MutableLiveData<String?>()
    val isRoomSpecificMode = MutableLiveData(false)
    val isFromRoomDetail = MutableLiveData(false)

    //Trả về danh sách dịch vụ hoặc mảng rỗng nếu chưa có dữ liệu
    val getServices get() = services.value?.data ?: arrayListOf()

    //Trả về danh sách dịch vụ riêng cho phòng hoặc mảng rỗng
    val getRoomSpecificServices get() = roomSpecificServices.value?.data ?: arrayListOf()
}