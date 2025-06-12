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
    //Trạng thái lọc người thuê (mặc định là ACTIVE - đang hoạt động)
    val filterState = MutableLiveData<NguoiThueEntity.Status>(NguoiThueEntity.Status.ACTIVE)
    //Chuỗi tìm kiếm người thuê
    val searchText = MutableLiveData<String>("")
    //Danh sách người thuê
    val tenants = MutableLiveData<Resource<List<Tenant>>>()

    //Thao tác CRUD người thuê
    val currentTenant = MutableLiveData<Tenant>()
    val updateTenant = MutableLiveData<Resource<Tenant>>()
    val updateCurrentUser = MutableLiveData<Resource<CommonUser>>()
    val deleteTenant = MutableLiveData<Resource<Boolean>>()

    val availableRooms = MutableLiveData<Resource<List<Room>>>()

    //Trả về danh sách người thuê đã được lọc theo:
    //Trạng thái hoạt động (filterState)
    //Tên chứa searchText hoặc số điện thoại chứa searchText
    val getListTenantByStateSearch: List<Tenant> get() = (tenants.value?.data ?: arrayListOf()).filter{
        it.status == filterState.value?.value
                && (it.fullName.containsSearch(searchText.value ?: "")
                || it.phoneNumber?.containsSearch(searchText.value ?: "") == true)
    }
}