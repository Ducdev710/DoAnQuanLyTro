package com.app.motel.feature.handleBill.viewmodel

import android.icu.util.Calendar
import androidx.lifecycle.MutableLiveData
import com.app.motel.common.service.DateConverter
import com.app.motel.common.service.DateConverter.toCalendar
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.entity.HoaDonEntity
import com.app.motel.data.model.Bill
import com.app.motel.data.model.Resource

class HandleBillState: AppViewLiveData {
    // Lưu trạng thái lọc theo tình trạng thanh toán, mặc định là "đã thanh toán"
    val filterState = MutableLiveData(HoaDonEntity.STATUS_PAID)

    //Lưu thời gian hiện tại dùng để lọc hóa đơn theo tháng/năm
    val currentDate = MutableLiveData(DateConverter.getCurrentDateTime().toCalendar())

    // Danh sách hóa đơn
    val bills = MutableLiveData<Resource<List<Bill>>>()

    // Hóa đơn hiện tại đang được xử lý
    val currentBill = MutableLiveData<Bill>()

    // Theo dõi trạng thái của thao tác cập nhật hóa đơna
    val updateBill = MutableLiveData<Resource<Bill>>()

    /**
     * Nếu người dùng là admin, thêm điều kiện lọc theo tháng/năm hiện tại
     * Nếu không phải admin, chỉ lọc theo trạng thái thanh toán
     */
    fun getListBillByFilter(isAdmin: Boolean): List<Bill> = (bills.value?.data ?: arrayListOf()).filter {
        it.status == filterState.value
                && (!isAdmin || (it.month == currentDate.value!!.get(Calendar.MONTH) + 1
                && it.year == currentDate.value!!.get(Calendar.YEAR)
                ))
    }
}