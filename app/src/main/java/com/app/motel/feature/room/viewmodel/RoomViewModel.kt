package com.app.motel.feature.room.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.app.motel.common.utils.toStringMoney
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.entity.PhongEntity
import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.Complaint
import com.app.motel.data.model.Contract
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.model.Service
import com.app.motel.data.model.Tenant
import com.app.motel.data.repository.BoardingHouseRepository
import com.app.motel.data.repository.ComplaintRepository
import com.app.motel.data.repository.ContractRepository
import com.app.motel.data.repository.RoomRepository
import com.app.motel.data.repository.ServiceRepository
import com.app.motel.data.repository.TenantRepository
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val serviceRepository: ServiceRepository,
    private val contractRepository: ContractRepository,
    private val tenantRepository: TenantRepository,
    private val complaintRepository: ComplaintRepository,
    private val boardingHouseRepository: BoardingHouseRepository,
    val userController: UserController,
): AppBaseViewModel<RoomViewState, RoomViewAction, RoomViewEvent>(
    RoomViewState()
) {
    // Lưu trữ ID khu trọ hiện tại đang được duyệt
    private var currentBrowsingBoardingHouseId: String? = null

    override fun handle(action: RoomViewAction) {
    }

    fun setStateRoomListData(status: PhongEntity.Status?){
        liveData.currentRoomState.postValue(Resource.Success(status))
    }

    fun initRoomDetail(item: Room?){
        liveData.currentRoom.value = Resource.Success(item)

        viewModelScope.launch {
            val roomFind = roomRepository.getRoomById(item?.id ?: "")
            if (roomFind?.id == null){
                return@launch
            }

            val contract: Contract? = contractRepository.getContractActiveByRoomId(roomFind.id)
            val services: Resource<List<Service>> = serviceRepository.getServiceByRoom(roomFind.areaId ?: "", roomFind.id)
            val tenants: List<Tenant> = tenantRepository.getTenantsByRoomId(roomFind.id)
            val boardingHouse: Resource<BoardingHouse> = boardingHouseRepository.getBoardingHouseById(roomFind.areaId ?: "")
            Log.e("RoomViewModel", "contract: ${contract}")
            roomFind.listService = services.data
            roomFind.contract = contract
            roomFind.boardingHouse = boardingHouse.data
            roomFind.tenants = tenants.map {
                it.room = roomFind
                it.contract = contract
                it
            }

            liveData.currentRoom.value = Resource.Success(roomFind)

            // Lưu lại ID khu trọ của phòng này để dùng sau khi gửi yêu cầu thuê
            if (roomFind.areaId != null) {
                currentBrowsingBoardingHouseId = roomFind.areaId
            }
        }
    }

    fun clearStateCreate(){
        liveData.currentRoom.postValue(Resource.Initialize())
        liveData.updateRoom.postValue(Resource.Initialize())
        liveData.deleteRoom.postValue(Resource.Initialize())
    }

    fun getRoom(){
        liveData.rooms.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val rooms = userController.state.getCurrentUser.let {
                    // Lấy phòng theo khu trọ và trạng thái
                    if(it?.isAdmin == true){
                        val boardingHouseId = userController.state.currentBoardingHouseId
                        val roomState = liveData.currentRoomState.value?.data
                        roomRepository.geRoomBytBoardingHouseId(boardingHouseId, roomState)

                    }else{
                        val roomState = liveData.currentRoomState.value?.data
                        if(roomState == PhongEntity.Status.EMPTY){ // Nếu đang xem phòng trống: hiển thị phòng có thể thuê
                            return@let roomRepository.getRoomByStatus(PhongEntity.Status.EMPTY)
                        }
                        // Hiển thị phòng đang thuê
                        val userId = userController.state.currentUserId
                        roomRepository.getCurrentRoomRentByTenantId(userId)
                    }
                }

                liveData.rooms.postValue(Resource.Success(rooms))
            }catch (e: Exception){
                liveData.rooms.postValue(Resource.Error(message = e.toString()))
            }
        }
    }

    fun updateRoom(
        room: Room?,
        nameRoom: String?,
        area: String?,
        maxTenant: String?,
        priceRoom: String?,
        note: String? = null,
        repairNote: String? = null
    ){
        liveData.updateRoom.postValue(Resource.Loading())
        val currentUser = userController.state.currentUser.value?.data
        when {
            currentUser == null || !currentUser.isAdmin -> {
                liveData.updateRoom.postValue(Resource.Error(message = "Bạn không có quyền sửa"))
                return
            }
            room?.id == null -> {
                liveData.updateRoom.postValue(Resource.Error(message = "Không tìm thấy phòng thuê"))
                return
            }
            nameRoom.isNullOrBlank() -> {
                liveData.updateRoom.postValue(Resource.Error(message = "Tên phòng không được để trống"))
                return
            }
            priceRoom.isNullOrBlank() -> {
                liveData.updateRoom.postValue(Resource.Error(message = "Giá phòng không được để trống"))
                return
            }
            maxTenant?.isNotEmpty() == true && (maxTenant.toIntOrNull() ?: 0) < (room.tenants?.size ?: 0)-> {
                liveData.updateRoom.postValue(Resource.Error(message = "Số lượng người thuê tối đa phải lớn hơn số lượng người thuê hiện tại"))
                return
            }
        }

        viewModelScope.launch {
            val roomUpdate = room!!.copy(
                roomName = nameRoom ?: "",
                area = area?.toDoubleOrNull(),
                maxOccupants = maxTenant?.toIntOrNull(),
                rentalPrice = priceRoom.toStringMoney(),
                note = note,
                repairNote = repairNote
            )

            val roomUpdated = roomRepository.updateRoom(roomUpdate)
            if(roomUpdated.isSuccess()){
                initRoomDetail(roomUpdated.data)
            }
            liveData.updateRoom.postValue(roomUpdated)
        }
    }

    fun deleteRoom(roomDelete: Room?){
        liveData.deleteRoom.postValue(Resource.Loading())
        val currentUser = userController.state.currentUser.value?.data
        when {
            currentUser == null || !currentUser.isAdmin -> {
                liveData.deleteRoom.postValue(Resource.Error(message = "Bạn không có quyền xóa"))
                return
            }
            roomDelete == null -> {
                liveData.deleteRoom.postValue(Resource.Error(message = "Không tìm thấy phòng"))
                return
            }
            roomDelete.isRenting || roomDelete.contract != null -> {
                liveData.deleteRoom.postValue(Resource.Error(message = "Phòng đang có hợp đồng không thể xóa"))
                return
            }
        }

        viewModelScope.launch {
            val roomDeleted = roomRepository.deleteRoom(roomDelete!!)

            if(roomDeleted.isSuccess()){
                val tenantRenting = tenantRepository.getTenantsByRoomId(roomDelete.id)
                tenantRenting.forEach {
                    tenantRepository.updateTenant(it.copy(roomId = null))
                }
            }
            liveData.deleteRoom.postValue(roomDeleted)
        }
    }

    fun createRoom(
        nameRoom: String?,
        area: String?,
        maxTenant: String?,
        priceRoom: String?,
        note: String? = null
    ){
        liveData.createRoom.postValue(Resource.Loading())

        val currentUser = userController.state.currentUser.value?.data
        val currentBoardingHouse = userController.state.getCurrentBoardingHouse
        when {
            currentUser == null || !currentUser.isAdmin -> {
                liveData.createRoom.postValue(Resource.Error(message = "Bạn không có quyền tạo"))
                return
            }
            currentBoardingHouse?.id == null -> {
                liveData.createRoom.postValue(Resource.Error(message = "Không tìm thấy khu trọ của bạn"))
                return
            }
            nameRoom.isNullOrBlank() -> {
                liveData.createRoom.postValue(Resource.Error(message = "Tên phòng không được để trống"))
                return
            }
            priceRoom.isNullOrBlank() -> {
                liveData.createRoom.postValue(Resource.Error(message = "Giá phòng không được để trống"))
                return
            }
        }

        viewModelScope.launch {
            val roomUpdate = Room(
                roomName = nameRoom ?: "",
                area = area?.toDoubleOrNull(),
                maxOccupants = maxTenant?.toIntOrNull(),
                rentalPrice = priceRoom.toStringMoney(),
                areaId = currentBoardingHouse?.id,
                note = note
            )

            val roomUpdated = roomRepository.createRoom(roomUpdate)
            if(roomUpdated.isSuccess()){
                initRoomDetail(roomUpdated.data)
            }
            liveData.createRoom.postValue(roomUpdated)
        }
    }

    fun rentRoom(room: Room){
        viewModelScope.launch {
            liveData.rentRoom.postValue(Resource.Loading())
            val currentUser = userController.state.currentUser.value?.data

            when {
                currentUser == null -> {
                    liveData.rentRoom.postValue(Resource.Error(message = "Không tìm thấy người dùng"))
                    return@launch
                }
                room.isRenting -> {
                    liveData.rentRoom.postValue(Resource.Error(message = "Phòng đang có người thuê"))
                    return@launch
                }
                contractRepository.getContractActiveByTenantId(currentUser.id) != null -> {
                    liveData.rentRoom.postValue(Resource.Error(message = "Hiện bạn đang thuê phòng khác"))
                    return@launch
                }
            }

            val rentRoomInsert = Complaint(
                title = "Yêu cầu thuê phòng",
                content = "Tôi muốn thuê phòng ${room.roomName}",
                submittedBy = currentUser?.id,
                roomId = room.id,
            )

            val rentRoom = complaintRepository.createRequireRentRoom(rentRoomInsert)
            liveData.rentRoom.postValue(rentRoom)

            // Cập nhật lại danh sách phòng trống của khu trọ hiện tại
            if (room.areaId != null) {
                refreshRoomsByBoardingHouse(room.areaId)
            } else if (currentBrowsingBoardingHouseId != null) {
                refreshRoomsByBoardingHouse(currentBrowsingBoardingHouseId!!)
            }
        }
    }

    fun refreshRoomsByBoardingHouse(boardingHouseId: String) {
        viewModelScope.launch {
            try {
                val rooms = roomRepository.getAvailableRoomsByBoardingHouseId(boardingHouseId)
                val boardingHouseResource = boardingHouseRepository.getBoardingHouseById(boardingHouseId)
                val boardingHouse = boardingHouseResource.data

                // Gắn thông tin khu trọ vào mỗi phòng
                val enrichedRooms = rooms.map { room ->
                    room.boardingHouse = boardingHouse
                    room
                }

                liveData.rooms.postValue(Resource.Success(enrichedRooms))
            } catch (e: Exception) {
                Log.e("RoomViewModel", "Error refreshing rooms: ${e.message}")
                // Giữ nguyên trạng thái danh sách phòng hiện tại
            }
        }
    }

    fun loadRoomsByLandlordId(landlordId: String, boardingHouseId: String? = null) {
        viewModelScope.launch {
            liveData.rooms.postValue(Resource.Loading())
            try {
                val rooms = if (boardingHouseId != null) {
                    // If boardingHouseId is provided, only get rooms from that specific boarding house
                    currentBrowsingBoardingHouseId = boardingHouseId
                    roomRepository.getAvailableRoomsByBoardingHouseId(boardingHouseId)
                } else {
                    // Otherwise get all available rooms from the landlord
                    roomRepository.getAvailableRoomsByLandlordId(landlordId)
                }
                liveData.rooms.postValue(Resource.Success(rooms))
            } catch (e: Exception) {
                liveData.rooms.postValue(Resource.Error(message = e.message ?: "Không thể tải danh sách phòng"))
            }
        }
    }

    fun loadEmptyRoomsForTenant(tenant: Tenant) {
        viewModelScope.launch {
            liveData.rooms.postValue(Resource.Loading())
            try {
                val currentRentedRoomId = tenant.roomId

                if (currentRentedRoomId != null) {
                    // Lấy thông tin phòng hiện tại của người thuê
                    val currentRoom = roomRepository.getRoomById(currentRentedRoomId)

                    if (currentRoom != null && currentRoom.areaId != null) {
                        // Lưu lại ID khu trọ hiện tại
                        currentBrowsingBoardingHouseId = currentRoom.areaId

                        // Lấy thông tin khu trọ từ phòng hiện tại
                        val boardingHouseResource = boardingHouseRepository.getBoardingHouseById(currentRoom.areaId)
                        val boardingHouse = boardingHouseResource.data

                        // Lấy danh sách phòng trống trong cùng khu trọ
                        val roomsInSameBoardingHouse = roomRepository.getAvailableRoomsByBoardingHouseId(currentRoom.areaId)

                        // Attach boarding house to each room without modifying areaId
                        val enrichedRooms = roomsInSameBoardingHouse.map { room ->
                            // Create a new room with the boarding house info
                            room.boardingHouse = boardingHouse
                            room
                        }

                        liveData.rooms.postValue(Resource.Success(enrichedRooms))
                    } else if (tenant.landlordId != null) {
                        // Fallback to landlord's rooms
                        loadRoomsByLandlordId(tenant.landlordId)
                    } else {
                        // Last resort: all empty rooms
                        val allEmptyRooms = roomRepository.getRoomByStatus(PhongEntity.Status.EMPTY)
                        liveData.rooms.postValue(Resource.Success(allEmptyRooms))
                    }
                } else {
                    // Với trường hợp người thuê không có phòng hiện tại
                    if (tenant.boardingHouseId != null) {
                        // Nếu người thuê có boarding house ID,
                        // Chỉ lấy các phòng trống trong boarding house đó
                        currentBrowsingBoardingHouseId = tenant.boardingHouseId

                        val boardingHouseResource = boardingHouseRepository.getBoardingHouseById(tenant.boardingHouseId)
                        val boardingHouse = boardingHouseResource.data
                        val roomsInBoardingHouse = roomRepository.getAvailableRoomsByBoardingHouseId(tenant.boardingHouseId)

                        // Attach boarding house info to each room
                        val enrichedRooms = roomsInBoardingHouse.map { room ->
                            room.boardingHouse = boardingHouse
                            room
                        }

                        liveData.rooms.postValue(Resource.Success(enrichedRooms))
                    } else if (tenant.landlordId != null) {
                        // Nếu người thuê có chủ nhà ID, không có boarding house ID,
                        // lấy tất cả các phòng trống của chủ nhà đó
                        loadRoomsByLandlordId(tenant.landlordId)
                    } else {
                        // Nếu không có thông tin nào về khu trọ hay chủ nhà, lây tất cả các phòng trống trong CSDL
                        val allEmptyRooms = roomRepository.getRoomByStatus(PhongEntity.Status.EMPTY)
                        liveData.rooms.postValue(Resource.Success(allEmptyRooms))
                    }
                }
            } catch (e: Exception) {
                Log.e("RoomViewModel", "Error loading rooms for tenant: ${e.message}")
                liveData.rooms.postValue(Resource.Error(message = e.message ?: "Không thể tải danh sách phòng"))

                // Try to recover by showing all empty rooms
                try {
                    val allEmptyRooms = roomRepository.getRoomByStatus(PhongEntity.Status.EMPTY)
                    liveData.rooms.postValue(Resource.Success(allEmptyRooms))
                } catch (e2: Exception) {
                    // Give up
                    liveData.rooms.postValue(Resource.Error(message = "Không thể tải danh sách phòng"))
                }
            }
        }
    }
}