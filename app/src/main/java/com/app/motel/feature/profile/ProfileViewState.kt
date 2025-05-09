package com.app.motel.feature.profile

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.CommonUser
import com.app.motel.data.model.Resource

class ProfileViewState : AppViewLiveData {
    val currentUser = MutableLiveData<Resource<CommonUser>>()
    val currentBoardingHouse = MutableLiveData<Resource<BoardingHouse>>()

    val isAdmin: Boolean get () = currentUser.value?.data?.isAdmin == true
    val currentUserId: String get() = currentUser.value?.data?.id ?: ""
    val currentBoardingHouseId: String get() = currentBoardingHouse.value?.data?.id ?: ""
    val getCurrentUser: CommonUser? get() = currentUser.value?.data
    val getCurrentBoardingHouse: BoardingHouse? get() = currentBoardingHouse.value?.data
}