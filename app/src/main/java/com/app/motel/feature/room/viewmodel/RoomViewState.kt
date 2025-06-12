package com.app.motel.feature.room.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.common.utils.containsSearch
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.entity.PhongEntity
import com.app.motel.data.model.Complaint
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room

class RoomViewState: AppViewLiveData {
    //Tìm kiếm và lọc
    val searchText = MutableLiveData<String>()
    val currentRoomState = MutableLiveData<Resource<PhongEntity.Status>>()

    //Danh sach phòng
    val rooms = MutableLiveData<Resource<List<Room>>>()

    // lọc phòng dựa trên văn bản tìm kiếm
    val roomsWithCurrentStateSearch: List<Room> get () = (rooms.value?.data ?: arrayListOf())
        .filter { item -> item.roomName.containsSearch(searchText.value ?: "") }

    // Trạng thái các thao tác CRUD
    val createRoom = MutableLiveData<Resource<Room>>()
    val updateRoom = MutableLiveData<Resource<Room>>()
    val deleteRoom = MutableLiveData<Resource<Room>>()
    val currentRoom = MutableLiveData<Resource<Room>>()

    // Trạng thái yêu cầu thuê phòng
    val rentRoom = MutableLiveData<Resource<Complaint>>()
}