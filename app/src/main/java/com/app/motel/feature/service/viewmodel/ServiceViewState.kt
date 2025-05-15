package com.app.motel.feature.service.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.model.Service

class ServiceViewState: AppViewLiveData {
    val services = MutableLiveData<Resource<List<Service>>>()
    val createService = MutableLiveData<Resource<Service>>()
    val updateRoomServices = MutableLiveData<Resource<List<Room>>>()
    val roomSpecificServices = MutableLiveData<Resource<List<Service>>>()

    val currentService = MutableLiveData<Service?>()
    val selectedRoomId = MutableLiveData<String?>()
    val isRoomSpecificMode = MutableLiveData(false)
    val isFromRoomDetail = MutableLiveData(false)

    val getServices get() = services.value?.data ?: arrayListOf()
    val getRoomSpecificServices get() = roomSpecificServices.value?.data ?: arrayListOf()
}