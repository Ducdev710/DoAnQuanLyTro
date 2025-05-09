package com.app.motel.feature.notify.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.entity.KhieuNaiEntity
import com.app.motel.data.model.Complaint
import com.app.motel.data.model.Notification
import com.app.motel.data.model.Resource

class NotifyViewState: AppViewLiveData {
    var isAdmin = false

    val complaints = MutableLiveData<List<Complaint>>()
    val currentTabType = MutableLiveData<KhieuNaiEntity.Type>(KhieuNaiEntity.Type.APPLICATION)

    val notifications = MutableLiveData<List<Notification>>()
    val currentTabGeneral = MutableLiveData<Boolean>(true)

    val currentHandleComplaint = MutableLiveData<Complaint>()
    val updateComplaint = MutableLiveData<Resource<Complaint>>()

    val getNotifyAdmin: List<Complaint> get () = complaints.value?.filter {
        it.type == currentTabType.value?.value
    }?.reversed() ?: arrayListOf()

    val getNotifyUser: List<Notification> get () = notifications.value?.filter {
        currentTabGeneral.value == true && it.phongId == null
        || currentTabGeneral.value == false && it.phongId != null
    }?.reversed() ?: arrayListOf()
}