package com.app.motel.feature.boardingHouse.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.Resource

class BoardingHouseState : AppViewLiveData {

    //isUpdateBoardingHousetrả về true nếu đang ở chế độ cập nhật (có khu trọ hiện tại)
    //phân biệt giữa chế độ tạo mới và cập nhật khu trọ
    val isUpdateBoardingHouse get () = currentBoardingHouse.value != null

    val currentBoardingHouse = MutableLiveData<BoardingHouse>()
    val saveBoardingHouse = MutableLiveData<Resource<BoardingHouse>>()

    val boardingHouse: MutableLiveData<Resource<List<BoardingHouse>>> = MutableLiveData()
}