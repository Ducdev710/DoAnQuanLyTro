package com.app.motel.feature.handleContract.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.entity.HopDongEntity
import com.app.motel.data.model.Contract
import com.app.motel.data.model.Resource

class HandleContractViewState: AppViewLiveData {
    val isAdmin = MutableLiveData<Boolean>(false)

    val contracts = MutableLiveData<Resource<List<Contract>>>()

    //Trạng thái hợp đồng hiện tại đang được chọn (đã/chưa thanh toán, sắp hết hạn)
    val currentStateContract = MutableLiveData<Contract.State>()

    //Trạng thái hoạt động của hợp đồng (active/inactive), mặc định là ACTIVE
    val currentStateActiveContract = MutableLiveData<Int>(HopDongEntity.ACTIVE)

    //Lưu trữ kết quả của các thao tác cập nhật hợp đồng (tạo mới, gia hạn, kết thúc)
    val updateContract = MutableLiveData<Resource<Contract>>()

    //Lọc danh sách hợp đồng theo trạng thái hiện tại và chỉ lấy các hợp đồng đang hoạt động
    val getContractToState: List<Contract>
        get() = contracts.value?.data?.filter {
            it.state == currentStateContract.value && it.isActive == HopDongEntity.ACTIVE
        } ?: arrayListOf()

    //Lọc danh sách hợp đồng theo trạng thái hoạt động (active/inactive)
    val getContractToActive: List<Contract>
        get() = contracts.value?.data?.filter {
            it.isActive == currentStateActiveContract.value
        } ?: arrayListOf()
}