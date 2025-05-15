package com.app.motel.feature.tenant.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.common.utils.containsSearch
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.entity.NguoiThueEntity
import com.app.motel.data.model.CommonUser
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.model.Tenant

class TenantState: AppViewLiveData {
    val filterState = MutableLiveData<NguoiThueEntity.Status>(NguoiThueEntity.Status.ACTIVE)
    val searchText = MutableLiveData<String>("")
    val tenants = MutableLiveData<Resource<List<Tenant>>>()

    val currentTenant = MutableLiveData<Tenant>()
    val updateTenant = MutableLiveData<Resource<Tenant>>()
    val updateCurrentUser = MutableLiveData<Resource<CommonUser>>()
    val deleteTenant = MutableLiveData<Resource<Boolean>>()
    val availableRooms = MutableLiveData<Resource<List<Room>>>()

    val getListTenantByStateSearch: List<Tenant> get() = (tenants.value?.data ?: arrayListOf()).filter{
        it.status == filterState.value?.value
                && (it.fullName.containsSearch(searchText.value ?: "")
                || it.phoneNumber?.containsSearch(searchText.value ?: "") == true)
    }
}