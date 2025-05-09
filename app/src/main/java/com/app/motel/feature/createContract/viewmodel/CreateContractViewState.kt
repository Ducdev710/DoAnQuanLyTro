package com.app.motel.feature.createContract.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.Contract
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.model.Tenant

class CreateContractViewState: AppViewLiveData {

    var currentRoomId: String? = null
    var currentTenantId: String? = null
    val createContract = MutableLiveData<Resource<Contract>>()
    val rooms = MutableLiveData<Resource<List<Room>>>()
    val tenantNotRented = MutableLiveData<Resource<List<Tenant>>>()

    val roomsNotRented: List<Room> get () = rooms.value?.data?.filter { item -> item.isEmpty} ?: arrayListOf()

}