package com.app.motel.feature.complaint.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.model.Complaint
import com.app.motel.data.model.Resource

class ComplaintState: AppViewLiveData {
    val complains = MutableLiveData<List<Complaint>>()

    val currentComplain = MutableLiveData<Complaint>()
    val updateComplaint = MutableLiveData<Resource<Complaint>>(Resource.Initialize())
}