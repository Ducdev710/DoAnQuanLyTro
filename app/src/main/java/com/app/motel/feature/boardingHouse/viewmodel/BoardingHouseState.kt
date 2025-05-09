package com.app.motel.feature.boardingHouse.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.Resource

class BoardingHouseState : AppViewLiveData {
    val isUpdateBoardingHouse get () = currentBoardingHouse.value != null

    val currentBoardingHouse = MutableLiveData<BoardingHouse>()
    val saveBoardingHouse = MutableLiveData<Resource<BoardingHouse>>()

    val boardingHouse: MutableLiveData<Resource<List<BoardingHouse>>> = MutableLiveData()
}