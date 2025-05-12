package com.app.motel.feature.revenue.viewmodel

import android.icu.util.Calendar
import androidx.lifecycle.MutableLiveData
import com.app.motel.common.service.DateConverter
import com.app.motel.common.service.DateConverter.toCalendar
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.entity.HoaDonEntity
import com.app.motel.data.model.Bill
import com.app.motel.data.model.Resource

class RevenueViewState: AppViewLiveData {
    val bills = MutableLiveData<Resource<List<Bill>>>()
    val currentDate = MutableLiveData(DateConverter.getCurrentDateTime().toCalendar())
    val filterState = MutableLiveData(HoaDonEntity.STATUS_PAID)

    fun getListBillByFilter(isAdmin: Boolean): List<Bill> {
        return (bills.value?.data ?: emptyList()).filter {
            it.status == filterState.value
                    && (!isAdmin || (it.month == currentDate.value!!.get(Calendar.MONTH) + 1
                    && it.year == currentDate.value!!.get(Calendar.YEAR)))
        }
    }
}