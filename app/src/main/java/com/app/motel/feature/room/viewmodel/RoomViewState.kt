package com.app.motel.feature.room.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.common.ultis.containsSearch
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.entity.PhongEntity
import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.Complaint
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room

class RoomViewState: AppViewLiveData {
    val searchText = MutableLiveData<String>()
    val currentRoomState = MutableLiveData<Resource<PhongEntity.Status>>()
    val rooms = MutableLiveData<Resource<List<Room>>>()
    val roomsWithCurrentStateSearch: List<Room> get () = (rooms.value?.data ?: arrayListOf())
        .filter { item -> item.roomName.containsSearch(searchText.value ?: "") }

    val createRoom = MutableLiveData<Resource<Room>>()
    val updateRoom = MutableLiveData<Resource<Room>>()
    val deleteRoom = MutableLiveData<Resource<Room>>()
    val currentRoom = MutableLiveData<Resource<Room>>()

    val rentRoom = MutableLiveData<Resource<Complaint>>()
}