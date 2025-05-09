package com.app.motel.feature.createBill.viewmodel

import android.icu.util.Calendar
import androidx.lifecycle.viewModelScope
import com.app.motel.common.service.DateConverter
import com.app.motel.common.service.DateConverter.toCalendar
import com.app.motel.common.ultis.toMoney
import com.app.motel.common.ultis.toStringMoney
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.entity.HoaDonEntity
import com.app.motel.data.model.Bill
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.repository.BillRepository
import com.app.motel.data.repository.ContractRepository
import com.app.motel.data.repository.RoomRepository
import com.app.motel.data.repository.ServiceRepository
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateBillViewModel @Inject constructor(
    private val billRepository: BillRepository,
    private val contractRepository: ContractRepository,
    private val serviceRepository: ServiceRepository,
    private val roomRepository: RoomRepository,
    private val userController: UserController,
): AppBaseViewModel<CreateBillViewState, CreateBillViewAction, CreateBillViewEvent>(
    CreateBillViewState()
) {
    override fun handle(action: CreateBillViewAction) {
    }

    fun initForm(item: Room?) {
        liveData.currentRoom.postValue(Resource.Success(item))
        getPreviousBill(item?.id)
        getServiceRoom(item?.id, item?.areaId)
    }

    fun clearStateCreate(){
        liveData.currentRoom.postValue(Resource.Initialize())
        liveData.previousBill.postValue(Resource.Initialize())
        liveData.currentServiceRoom.postValue(Resource.Initialize())
        liveData.createBill.postValue(Resource.Initialize())
    }

    private fun getServiceRoom(roomId: String?, boardingHouseId: String?){
        if (roomId == null || boardingHouseId == null){
            liveData.currentServiceRoom.postValue(Resource.Error(message = "Không tìm thấy phòng thuê"))
            return
        }

        viewModelScope.launch {
            liveData.currentServiceRoom.postValue(serviceRepository.getServiceByRoom(boardingHouseId, roomId))
        }
    }

    private fun getPreviousBill(roomId: String?){
        if (roomId == null){
            liveData.previousBill.postValue(Resource.Error(message = "Không tìm thấy phòng thuê"))
            return
        }

        viewModelScope.launch {
            try {
                liveData.previousBill.postValue(billRepository.getPreviousBill(roomId))
            }catch (e: Exception){
                liveData.previousBill.postValue(Resource.Error(message = e.toString()))
            }
        }
    }

    fun getService(){
        liveData.rooms.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val boardingHouseId = userController.state.currentBoardingHouseId
                val boardingHouses = roomRepository.geRoomBytBoardingHouseId(boardingHouseId)
                liveData.rooms.postValue(Resource.Success(boardingHouses))
            }catch (e: Exception){
                liveData.rooms.postValue(Resource.Error(message = e.toString()))
            }
        }
    }

    fun createBill(
        createdDate: String?,
        oldElectricityMeter: Int?,
        newElectricityMeter: Int?,
        oldWaterMeter: Int?,
        newWaterMeter: Int?,
        servicePrice: Int?,
        discount: Int?,
        note: String?,
    ){
        liveData.createBill.postValue(Resource.Loading())

        val room = liveData.currentRoom.value?.data
        val currentUser = userController.state.currentUser.value?.data
        val createDate = DateConverter.localStringToDate(createdDate)
        when {
            currentUser == null || !currentUser.isAdmin -> {
                liveData.createBill.postValue(Resource.Error(message = "Bạn không có quyền tạo"))
                return
            }
            room?.id == null -> {
                liveData.createBill.postValue(Resource.Error(message = "Không tìm thấy phòng thuê"))
                return
            }
            createdDate.isNullOrBlank() -> {
                liveData.createBill.postValue(Resource.Error(message = "Thời gian lập hóa đơn là bắt buộc"))
                return
            }
            createDate == null -> {
                liveData.createBill.postValue(Resource.Error(message = "Thời gian lập hóa đơn không hợp lệ"))
                return
            }
            DateConverter.getCurrentDateTime().before(createDate) -> {
                liveData.createBill.postValue(Resource.Error(message = "Thời gian lập hóa đơn quá ngày hiện tại"))
                return
            }
            newElectricityMeter == null-> {
                liveData.createBill.postValue(Resource.Error(message = "Số điện mới là bắt buộc"))
                return
            }
            newElectricityMeter <= (oldElectricityMeter ?: 0) -> {
                liveData.createBill.postValue(Resource.Error(message = "Số điện mới không được thấp hơn số điện cũ"))
                return
            }
            newWaterMeter == null -> {
                liveData.createBill.postValue(Resource.Error(message = "Số nước mới là bắt buộc"))
                return
            }
            newWaterMeter <= (oldWaterMeter ?: 0) -> {
                liveData.createBill.postValue(Resource.Error(message = "Số nước mới không được thấp hơn số nước cũ"))
                return
            }
        }
        viewModelScope.launch {
            val checkBillCurrentMonth = billRepository.checkBillCreateDate(room?.id ?: "", createDate!!.toCalendar())
            if(checkBillCurrentMonth.isError()){
                liveData.createBill.postValue(checkBillCurrentMonth)
                return@launch
            }

            val waterUsed = newWaterMeter!! - (oldWaterMeter ?: 0)
            val electricityUsed = newElectricityMeter!! - (oldElectricityMeter ?: 0)
            val total = room?.rentalPrice.toMoney().toDouble() +
                    waterUsed * HoaDonEntity.PRICE_ELECTRICITY +
                    electricityUsed * HoaDonEntity.PRICE_WATER +
                    (servicePrice ?: 0) -
                    (discount ?: 0)

            val newBill = Bill(
                name = room?.roomName,
                createdDate = createdDate,
                roomPrice = room?.rentalPrice.toMoney().toDouble(),
                waterIndex = newWaterMeter,
                waterUsed = waterUsed,
                electricityIndex = newElectricityMeter,
                electricityUsed = electricityUsed,
                serviceFee = servicePrice.toStringMoney(),
                discount = discount.toStringMoney(),
                totalAmount = total.toInt().toStringMoney(),
                roomId = room?.id ?: "",
                month = createDate.toCalendar().get(Calendar.MONTH) + 1,
                year = createDate.toCalendar().get(Calendar.YEAR),
                note = note,
            )

            val contractCreated = billRepository.createBill(newBill)
            liveData.createBill.postValue(contractCreated)
        }
    }
}