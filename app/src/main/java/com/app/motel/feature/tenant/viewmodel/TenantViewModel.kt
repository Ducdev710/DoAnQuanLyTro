package com.app.motel.feature.tenant.viewmodel

import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.entity.NguoiThueEntity
import com.app.motel.data.model.CommonUser
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.model.Tenant
import com.app.motel.data.model.User
import com.app.motel.data.repository.ProfileRepository
import com.app.motel.data.repository.RoomRepository
import com.app.motel.data.repository.TenantFilterType
import com.app.motel.data.repository.TenantRepository
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import javax.inject.Inject

class TenantViewModel @Inject constructor(
    private val tenantRepository: TenantRepository,
    private val profileRepository: ProfileRepository,
    private val roomRepository: RoomRepository,
    private val userController: UserController
): AppBaseViewModel<TenantState, TenantViewAction, TenantViewEvent>(TenantState()) {
    override fun handle(action: TenantViewAction) {

    }

    fun getTenants() {
        viewModelScope.launch {
            val currentUser = userController.state.currentUser.value?.data
            val currentBoardingHouse = userController.state.getCurrentBoardingHouse

            liveData.tenants.postValue(Resource.Loading())

            try {
                if (currentUser?.isAdmin == true) {
                    val tenants = if (currentBoardingHouse != null) {
                        // Get tenants for the current boarding house only
                        tenantRepository.getTenantsByBoardingHouseId(currentBoardingHouse.id)
                    } else {
                        // If no boarding house is selected, show all tenants for this landlord
                        tenantRepository.getTenantsByLandlordId(currentUser.id)
                    }
                    liveData.tenants.postValue(Resource.Success(tenants))
                } else {
                    val allTenants = tenantRepository.getTenants()
                    liveData.tenants.postValue(allTenants)
                }
            } catch (e: Exception) {
                liveData.tenants.postValue(Resource.Error(message = e.message ?: "Không thể tải danh sách người thuê"))
            }
        }
    }

    // Updated function to load tenants with filtering and landlord restriction
    fun loadTenants(filterType: TenantFilterType = TenantFilterType.ALL) {
        viewModelScope.launch {
            liveData.tenants.postValue(Resource.Loading())
            try {
                val currentUser = userController.state.currentUser.value?.data
                val tenants = if (currentUser?.isAdmin == true) {
                    // Filter tenants by both type and landlord ID
                    tenantRepository.getTenantsByFilterAndLandlord(filterType, currentUser.id)
                } else {
                    tenantRepository.getTenantsByFilter(filterType)
                }
                liveData.tenants.postValue(Resource.Success(tenants))
            } catch (e: Exception) {
                liveData.tenants.postValue(Resource.Error(message = e.message ?: "Không thể tải danh sách người thuê"))
            }
        }
    }

    fun loadAvailableRoomsForTenant(tenant: Tenant) {
        viewModelScope.launch {
            if (tenant.landlordId != null) {
                val rooms = roomRepository.getAvailableRoomsByLandlordId(tenant.landlordId)
                liveData.availableRooms.postValue(Resource.Success(rooms))
            } else {
                liveData.availableRooms.postValue(Resource.Error(message = "Không tìm thấy thông tin chủ nhà"))
            }
        }
    }

    fun initForm(tenant: Tenant?){
        liveData.currentTenant.postValue(tenant)
    }

    fun handleTenant(
        tenant: Tenant?,
        fullName: String?,
        state: String?,
        phoneNumber: String?,
        birthDay: String?,
        idCard: String?,
        homeTown: String?,
        username: String?,
        password: String?,
    ){
        liveData.updateTenant.postValue(Resource.Loading())
        val currentUser = userController.state.currentUser.value?.data
        val currentBoardingHouse = userController.state.getCurrentBoardingHouse

        when {
            currentUser == null || !currentUser.isAdmin -> {
                liveData.updateTenant.postValue(Resource.Error(message = "Bạn không có quyền tạo"))
                return
            }
            fullName.isNullOrBlank() -> {
                liveData.updateTenant.postValue(Resource.Error(message = "Họ tên là bắt buộc"))
                return
            }
            state.isNullOrBlank() -> {
                liveData.updateTenant.postValue(Resource.Error(message = "Trạng thái là bắt buộc"))
                return
            }
            username.isNullOrBlank() -> {
                liveData.updateTenant.postValue(Resource.Error(message = "Tên đăng nhập là bắt buộc"))
                return
            }
            password.isNullOrBlank() -> {
                liveData.updateTenant.postValue(Resource.Error(message = "Mật khẩu là bắt buộc"))
                return
            }
        }

        viewModelScope.launch {
            val isUpdate = tenant != null
            val tenantUpdate: Tenant = if(isUpdate) tenant!!.copy(
                fullName = fullName!!,
                status = state,
                phoneNumber = phoneNumber,
                birthDay = birthDay,
                idCard = idCard,
                homeTown = homeTown,
                username = username!!,
                password = password!!,
                landlordId = tenant.landlordId ?: currentUser?.id, // Fixed: added comma here
                boardingHouseId = currentBoardingHouse?.id
            ) else Tenant(
                id = "", // UUID will be generated in repository
                fullName = fullName!!,
                status = state,
                phoneNumber = phoneNumber,
                birthDay = birthDay,
                idCard = idCard,
                homeTown = homeTown,
                username = username!!,
                password = password!!,
                landlordId = currentUser?.id,
                boardingHouseId = currentBoardingHouse?.id // Added boarding house ID for new tenants
            )

            val result = if(isUpdate) tenantRepository.updateTenant(tenantUpdate)
            else tenantRepository.addTenant(tenantUpdate)

            liveData.updateTenant.postValue(result)
        }
    }

    fun changeStateTenant(
        tenant: Tenant?,
        isLock: Boolean,
    ){
        liveData.updateTenant.postValue(Resource.Loading())
        val currentUser = userController.state.currentUser.value?.data
        when {
            currentUser == null || !currentUser.isAdmin -> {
                liveData.updateTenant.postValue(Resource.Error(message = "Bạn không có quyền tạo"))
                return
            }
            tenant?.id == null -> {
                liveData.updateTenant.postValue(Resource.Error(message = "Người thuê không tồn tại"))
                return
            }
            tenant.landlordId != currentUser.id -> {
                liveData.updateTenant.postValue(Resource.Error(message = "Bạn không có quyền chỉnh sửa người thuê này"))
                return
            }
        }

        viewModelScope.launch {
            // Using the specific lock status update method
            val result = tenantRepository.updateTenantLockStatus(tenant!!, isLock)
            liveData.updateTenant.postValue(result)

            // Update the current tenant in the form if the operation was successful
            if (result.isSuccess()) {
                liveData.currentTenant.postValue(tenant.copy(isLock = isLock))
            }
        }
    }

    fun updateTenantRent(tenant: Tenant, room: Room?){
        liveData.updateTenant.postValue(Resource.Loading())
        val currentUser = userController.state.currentUser.value?.data
        when {
            currentUser == null || !currentUser.isAdmin -> {
                liveData.updateTenant.postValue(Resource.Error(message = "Bạn không có quyền tạo"))
                return
            }
            tenant.landlordId != currentUser.id -> {
                liveData.updateTenant.postValue(Resource.Error(message = "Bạn không có quyền chỉnh sửa người thuê này"))
                return
            }
        }
        viewModelScope.launch {
            val result = tenantRepository.updateTenantRentToRoom(tenant.id, room?.id)
            liveData.updateTenant.postValue(result)
        }
    }

    fun updateCurrentUser(
        currentUser: CommonUser?,
        fullName: String?,
        birthDay: String?,
        phoneNumber: String?,
        email: String?,
        homeTown: String?,
        idCard: String?,
        username: String?,
        password: String?,
        bankName: String? = null,
        accountNumber: String? = null
    ) {
        liveData.updateCurrentUser.postValue(Resource.Loading())
        when {
            currentUser?.child !is User && currentUser?.child !is Tenant -> {
                liveData.updateCurrentUser.postValue(Resource.Error(message = "Không tìm thấy thông tin hiện tại của bạn"))
                return
            }
            fullName.isNullOrBlank() -> {
                liveData.updateCurrentUser.postValue(Resource.Error(message = "Họ tên là bắt buộc"))
                return
            }
            currentUser.isAdmin && email.isNullOrBlank() -> {
                liveData.updateCurrentUser.postValue(Resource.Error(message = "Email không được để trống"))
                return
            }
            currentUser.isAdmin && !email!!.let { Patterns.EMAIL_ADDRESS.matcher(it).matches() } -> {
                liveData.updateCurrentUser.postValue(Resource.Error(message = "Lỗi định dạng email"))
                return
            }
            username.isNullOrBlank() -> {
                liveData.updateCurrentUser.postValue(Resource.Error(message = "Tên đăng nhập là bắt buộc"))
                return
            }
            password.isNullOrBlank() -> {
                liveData.updateCurrentUser.postValue(Resource.Error(message = "Mật khẩu là bắt buộc"))
                return
            }
        }

        viewModelScope.launch {
            val userUpdate: CommonUser = currentUser!!.copy(
                fullName = fullName!!,
                birthDay = birthDay,
                phoneNumber = phoneNumber,
                homeTown = homeTown,
                email = email,
                username = username,
                password = password,
                idCard = idCard
            )

            // If you need to pass bank info separately because it's not part of the copy method
            val result = if (currentUser.isAdmin) {
                profileRepository.updateCurrentUser(userUpdate, bankName, accountNumber)
            } else {
                profileRepository.updateCurrentUser(userUpdate)
            }

            liveData.updateCurrentUser.postValue(result)
        }
    }

    fun deleteTenant(tenantId: String) {
        viewModelScope.launch {
            try {
                val currentUser = userController.state.currentUser.value?.data
                // Get tenant first to check ownership
                val tenant = tenantRepository.getTenantsById(tenantId)

                if (tenant == null) {
                    liveData.deleteTenant.postValue(Resource.Error(message = "Không tìm thấy người thuê"))
                    return@launch
                }

                if (currentUser?.isAdmin != true || tenant.landlordId != currentUser.id) {
                    liveData.deleteTenant.postValue(Resource.Error(message = "Bạn không có quyền xóa người thuê này"))
                    return@launch
                }

                liveData.deleteTenant.postValue(Resource.Loading())
                val result = tenantRepository.deleteTenant(tenantId)
                liveData.deleteTenant.postValue(Resource.Success(true, "Xóa khách thuê thành công"))
            } catch (e: Exception) {
                liveData.deleteTenant.postValue(Resource.Error(message = e.message ?: "Xóa khách thuê thất bại"))
            }
        }
    }
}