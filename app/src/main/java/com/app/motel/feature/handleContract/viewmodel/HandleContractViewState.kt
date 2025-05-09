package com.app.motel.feature.handleContract.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.entity.HopDongEntity
import com.app.motel.data.model.Contract
import com.app.motel.data.model.Resource

class HandleContractViewState: AppViewLiveData {
    val isAdmin = MutableLiveData<Boolean>(false)

    val contracts = MutableLiveData<Resource<List<Contract>>>()

    val currentStateContract = MutableLiveData<Contract.State>()
    val currentStateActiveContract = MutableLiveData<Int>(HopDongEntity.ACTIVE)

    val updateContract = MutableLiveData<Resource<Contract>>()

    val getContractToState: List<Contract>
        get() = contracts.value?.data?.filter {
            it.state == currentStateContract.value && it.isActive == HopDongEntity.ACTIVE
        } ?: arrayListOf()
    val getContractToActive: List<Contract>
        get() = contracts.value?.data?.filter {
            it.isActive == currentStateActiveContract.value
        } ?: arrayListOf()
}