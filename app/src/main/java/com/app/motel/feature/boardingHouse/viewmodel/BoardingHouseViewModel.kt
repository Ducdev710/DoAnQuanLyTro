package com.app.motel.feature.boardingHouse.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.viewModelScope
import com.app.motel.common.utils.formatRoomName
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.repository.BoardingHouseRepository
import com.app.motel.data.repository.RoomRepository
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import javax.inject.Inject

class BoardingHouseViewModel @Inject constructor(
    private val boardingRepository: BoardingHouseRepository,
    private val roomRepository: RoomRepository,
    private val userController: UserController,
) : AppBaseViewModel<BoardingHouseState, BoardingHouseViewAction, BoardingHouseViewEvent>(
    BoardingHouseState()
) {

    override fun handle(action: BoardingHouseViewAction) {
    }

    @SuppressLint("DefaultLocale")
    fun saveBoardingHouse(
        name: String?,
        roomCount: Int?,
        address: String?,
    ){
        liveData.saveBoardingHouse.postValue(Resource.Loading())

        val currentUser = userController.state.currentUser.value?.data
        when {
            currentUser == null || !currentUser.isAdmin -> {
                liveData.saveBoardingHouse.postValue(Resource.Error(message = "Bạn không có quyền tạo"))
                return
            }
            name.isNullOrBlank() -> {
                liveData.saveBoardingHouse.postValue(Resource.Error(message = "Tên không được để trống"))
                return
            }
            address.isNullOrBlank() -> {
                liveData.saveBoardingHouse.postValue(Resource.Error(message = "Dịa chỉ không được để trống"))
                return
            }
        }

        viewModelScope.launch {
            val boardingHouseSave = if(liveData.isUpdateBoardingHouse) liveData.currentBoardingHouse.value!!.copy(
                name = name!!,
                address = address!!,
            ) else BoardingHouse(
                name = name!!,
                address = address!!,
                roomCount = roomCount,
                ownerId = currentUser!!.id,
            )

            val boardingHouseCreated = if(liveData.isUpdateBoardingHouse) boardingRepository.updateBoardingHouse(boardingHouseSave)
                else boardingRepository.createBoardingHouse(boardingHouseSave)

            if(!liveData.isUpdateBoardingHouse && boardingHouseCreated.data != null){
                userController.setCurrentBoardingHouse(boardingHouseCreated.data)
            }

            if(!liveData.isUpdateBoardingHouse
                && boardingHouseCreated.isSuccess()
                && (boardingHouseCreated.data?.roomCount ?: 0) > 0
                ){
                for (i in 1..boardingHouseCreated.data!!.roomCount!!) {
                    val newRoom = Room(
                        roomName = "Phòng ${i.formatRoomName()}",
                        rentalPrice = "2 000 000",
                        areaId = boardingHouseCreated.data.id,
                    )
                    roomRepository.createRoom(newRoom)
                }
            }
            liveData.saveBoardingHouse.postValue(boardingHouseCreated)
        }
    }

    fun deleteBoardingHouse(boardingHouse: BoardingHouse){
        val currentUser = userController.state.currentUser
        val currentBoardingHouse = userController.state.currentBoardingHouse

        when{
            currentUser.value?.data == null || !currentUser.value?.data!!.isAdmin ->{
                liveData.saveBoardingHouse.postValue(Resource.Error(message = "Bạn không có quyền xóa"))
                return
            }
            boardingHouse.id.isBlank() ->{
                liveData.saveBoardingHouse.postValue(Resource.Error(message = "Không tìm thấy khu trọ"))
                return
            }
            currentBoardingHouse.value?.data?.id == boardingHouse.id ->{
                liveData.saveBoardingHouse.postValue(Resource.Error(message = "Không thể xóa khu trọ đang sử dụng"))
                return
            }
        }

        viewModelScope.launch {
            liveData.saveBoardingHouse.postValue(Resource.Loading())
            val boardingHouseDeleted = boardingRepository.deleteBoardingHouse(boardingHouse)
            liveData.saveBoardingHouse.postValue(boardingHouseDeleted)
        }
    }

    fun getBoardingHouse(){
        val currentUser = userController.state.currentUser.value?.data
        when{
            currentUser == null || !currentUser.isAdmin ->{
                liveData.boardingHouse.postValue(Resource.Error(message = "Bạn không có quyền xem"))
                return
            }
            currentUser.id.isBlank() ->{
                liveData.boardingHouse.postValue(Resource.Error(message = "Không tìm thấy người dùng"))
                return
            }
        }
        viewModelScope.launch {
            liveData.boardingHouse.postValue(Resource.Loading())
            val boardingHouse = boardingRepository.getBoardingHouseByUserId(currentUser!!.id)
            liveData.boardingHouse.postValue(boardingHouse)
        }
    }

    fun initForm(boardingHouse: BoardingHouse?) {
        liveData.currentBoardingHouse.postValue(boardingHouse)
    }
}