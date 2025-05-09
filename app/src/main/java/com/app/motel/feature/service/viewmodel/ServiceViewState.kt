package com.app.motel.feature.service.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Service

class ServiceViewState: AppViewLiveData {
    val services = MutableLiveData<Resource<List<Service>>>()
    val createService = MutableLiveData<Resource<Service>>()

    val currentService = MutableLiveData<Service?>()

    val getServices get () = services.value?.data ?: arrayListOf()
}