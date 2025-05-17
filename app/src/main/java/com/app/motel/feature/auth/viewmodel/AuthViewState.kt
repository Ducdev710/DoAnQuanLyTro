package com.app.motel.feature.auth.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.model.CommonUser
import com.app.motel.data.model.Resource

class AuthViewState : AppViewLiveData {
    val login = MutableLiveData<Resource<CommonUser>>()
    val register = MutableLiveData<Resource<CommonUser>>()
    //val registerInit = MutableLiveData<Resource<String>>()
}