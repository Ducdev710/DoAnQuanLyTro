package com.app.motel.feature.rules.viewmodel

import androidx.lifecycle.viewModelScope
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.entity.QuyDinhEntity
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Rules
import com.app.motel.data.repository.RulesRepository
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import javax.inject.Inject

class RulesViewModel @Inject constructor(
    private val rulesRepository: RulesRepository,
    val userController: UserController,
): AppBaseViewModel<RulesViewState, RulesViewAction, RulesViewEvent>(
    RulesViewState()
) {
    override fun handle(action: RulesViewAction) {

    }

    fun getRules(){
        viewModelScope.launch {
            try {
                val rules = userController.state.isAdmin.let {
                    if(it){
                        rulesRepository.getRulesByBoardingHouseId(userController.state.currentBoardingHouseId)
                    }else{
                        // Lấy nội quy cho người thuê
                        val tenantRules = rulesRepository.getRulesByTenantId(userController.state.currentUserId)

                        // Lấy thông tin chủ trọ
                        val boardingHouseId = userController.state.currentBoardingHouseId
                        val landlordInfo = if (boardingHouseId.isNotEmpty()) {
                            rulesRepository.getLandlordInfoByBoardingHouseId(boardingHouseId)
                                ?: getLandlordInfo()
                        } else {
                            getLandlordInfo()
                        }

                        // Lấy thông tin về giá điện và nước của khu trọ
                        val boardingHouse = if (boardingHouseId.isNotEmpty()) {
                            rulesRepository.getBoardingHouseById(boardingHouseId)
                        } else null

                        // Store utility prices for tenant users
                        if (boardingHouse != null) {
                            liveData.utilityPrices.postValue(Resource.Success(
                                UtilityPrices(
                                    electricityPrice = boardingHouse.giaDien,
                                    waterPrice = boardingHouse.giaNuoc
                                )
                            ))
                        }

                        // Find any existing contact information rule
                        val contactRule = tenantRules.find {
                            it.title.contains("Thông tin liên hệ", ignoreCase = true)
                        }

                        if (contactRule != null) {
                            // Cập nhât nội dung của quy định liên hệ
                            val updatedRules = tenantRules.map { rule ->
                                if (rule.id == contactRule.id) {
                                    // Include electricity and water prices if available
                                    val utilityPrices = if (boardingHouse != null) {
                                        """
                                    
                                    Giá điện: ${formatPrice(boardingHouse.giaDien)} VNĐ/số
                                    Giá nước: ${formatPrice(boardingHouse.giaNuoc)} VNĐ/khối
                                    """.trimIndent()
                                    } else ""

                                    rule.copy(content = """
                                Số điện thoại: ${landlordInfo.phoneNumber}
                                Email: ${landlordInfo.email}
                                Ngân hàng: ${landlordInfo.bankName ?: ""}
                                Số tài khoản: ${landlordInfo.accountNumber ?: ""}$utilityPrices
                                """.trimIndent())
                                } else {
                                    rule
                                }
                            }
                            updatedRules
                        } else {
                            tenantRules
                        }
                    }
                }
                liveData.rules.postValue(Resource.Success(rules))

                // Lấy giá điện và nước của khu trọ cho tài khoản chủ nhà
                if (userController.state.isAdmin) {
                    userController.state.currentBoardingHouse.value?.data?.let { boardingHouse ->
                        liveData.utilityPrices.postValue(Resource.Success(
                            UtilityPrices(
                                electricityPrice = boardingHouse.giaDien,
                                waterPrice = boardingHouse.giaNuoc
                            )
                        ))
                    }
                }
            }catch (e: Exception){
                liveData.rules.postValue(Resource.Error(message = e.message ?: "Unknown error"))
            }
        }
    }

    private fun formatPrice(price: Int): String {
        return price.toString().chunked(3).joinToString(",")
    }

    // Lấy thông tin chủ trọ cho người thuê
    suspend fun getLandlordInfo(): LandlordInfo {
        try {
            // For tenant users, try to get landlord info from repository first
            if (!userController.state.isAdmin) {
                val boardingHouseId = userController.state.currentBoardingHouseId
                if (boardingHouseId.isNotEmpty()) {
                    val repoLandlordInfo = rulesRepository.getLandlordInfoByBoardingHouseId(boardingHouseId)
                    if (repoLandlordInfo != null) {
                        return repoLandlordInfo
                    }
                }
            }

            // Get the current user data
            val currentUser = userController.state.currentUser.value?.data

            // Try to get landlord based on relationship
            val landlordId = if (!userController.state.isAdmin) {
                rulesRepository.getLandlordIdForTenant(userController.state.currentUserId)
            } else null

            // If we have landlordId, get their info
            if (landlordId != null) {
                val landlordInfo = rulesRepository.getUserById(landlordId)
                if (landlordInfo != null) {
                    return LandlordInfo(
                        phoneNumber = landlordInfo.phoneNumber ?: "",
                        email = landlordInfo.email ?: "",
                        bankName = landlordInfo.bankName,
                        accountNumber = landlordInfo.accountNumber
                    )
                }
            }

            // Access the nested User object inside AdminUser
            val childUser = when {
                // For AdminUser type with child field
                currentUser != null && "AdminUser" in currentUser.javaClass.simpleName -> {
                    try {
                        val childField = currentUser.javaClass.getDeclaredField("child")
                        childField.isAccessible = true
                        childField.get(currentUser)
                    } catch (e: Exception) {
                        null
                    }
                }
                else -> currentUser
            }

            // Extract phone number from the child object
            val phoneNumber = when {
                childUser != null -> {
                    try {
                        val field = childUser.javaClass.getDeclaredField("phoneNumber")
                        field.isAccessible = true
                        field.get(childUser) as? String
                    } catch (e: Exception) {
                        null
                    }
                }
                else -> null
            } ?: ""

            // Extract email
            val email = when {
                childUser != null -> {
                    try {
                        val field = childUser.javaClass.getDeclaredField("email")
                        field.isAccessible = true
                        field.get(childUser) as? String
                    } catch (e: Exception) {
                        null
                    }
                }
                else -> null
            } ?: ""

            // Extract bank name
            val bankName = when {
                childUser != null -> {
                    try {
                        val field = childUser.javaClass.getDeclaredField("bankName")
                        field.isAccessible = true
                        field.get(childUser) as? String
                    } catch (e: Exception) {
                        null
                    }
                }
                else -> null
            } ?: ""

            // Extract account number
            val accountNumber = when {
                childUser != null -> {
                    try {
                        val field = childUser.javaClass.getDeclaredField("accountNumber")
                        field.isAccessible = true
                        field.get(childUser) as? String
                    } catch (e: Exception) {
                        null
                    }
                }
                else -> null
            } ?: ""

            // Only use defaults if we couldn't get any real data
            val info = LandlordInfo(
                phoneNumber = phoneNumber.ifEmpty { "0812569662" },
                email = email.ifEmpty { "ducvu@gmail.com" },
                bankName = bankName.ifEmpty { "Vietcombank" },
                accountNumber = accountNumber.ifEmpty { "1234567890" }
            )

            return info
        } catch (e: Exception) {
            return LandlordInfo(
                phoneNumber = "0812569662",
                email = "ducvu@gmail.com",
                bankName = "Vietcombank",
                accountNumber = "1234567890"
            )
        }
    }

    // Lấy thông tin khu trọ cho người thuê
    fun getTenantBoardingHouse() {
        viewModelScope.launch {
            try {
                if (!userController.state.isAdmin) {
                    val tenantId = userController.state.currentUserId
                    if (tenantId.isNotEmpty()) {
                        // Get boarding house for this tenant
                        val boardingHouseId = userController.state.currentBoardingHouseId
                        if (boardingHouseId.isNotEmpty()) {
                            val boardingHouse = rulesRepository.getBoardingHouseById(boardingHouseId)
                            if (boardingHouse != null) {
                                // Post utility prices to LiveData
                                liveData.utilityPrices.postValue(Resource.Success(
                                    UtilityPrices(
                                        electricityPrice = boardingHouse.giaDien,
                                        waterPrice = boardingHouse.giaNuoc
                                    )
                                ))
                            }
                        } else {
                            // Try to get boarding house by tenant ID if we don't have the ID
                            val boardingHouse = rulesRepository.getBoardingHouseByTenantId(tenantId)
                            if (boardingHouse != null) {
                                liveData.utilityPrices.postValue(Resource.Success(
                                    UtilityPrices(
                                        electricityPrice = boardingHouse.giaDien,
                                        waterPrice = boardingHouse.giaNuoc
                                    )
                                ))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Log error but don't crash
            }
        }
    }

    // Xử lý thêm, cập nhật hoặc xóa nội quy
    fun handleRules(rule: Rules, isUpdate: Boolean = true){
        val currentUser = userController.state.currentUser.value?.data

        when{
            currentUser?.isAdmin != true -> {
                liveData.addRules.postValue(Resource.Error(message = "Bạn không có quyền thêm nội quy"))
                return
            }
            rule.title.isBlank() -> {
                liveData.addRules.postValue(Resource.Error(message = "Tiêu đề không được để trống"))
                return
            }
            rule.content.isNullOrBlank() -> {
                liveData.addRules.postValue(Resource.Error(message = "Nội dung không được để trống"))
                return
            }
        }

        val rulesUpdate: ArrayList<Rules> = liveData.rules.value?.data as? ArrayList<Rules> ?: arrayListOf()

        val positionExits: Int = rulesUpdate.indexOfFirst { it.id == rule.id }
        if(isUpdate) {
            // update
            if(positionExits != -1) rulesUpdate[positionExits] = rule
            // add
            else rulesUpdate.add(rule.copy(boardingHouseId = userController.state.currentBoardingHouseId))
        }
        else {
            // delete
            if(positionExits != -1) rulesUpdate[positionExits] = rule.copy(status = QuyDinhEntity.STATUS_INACTIVE)
        }
        liveData.rules.postValue(Resource.Success(rulesUpdate))
        liveData.addRules.postValue(Resource.Success(rule))
    }

    // Lưu danh sách nội quy
    fun saveRules(rules: List<Rules>){
        val currentUser = userController.state.currentUser.value?.data

        when{
            currentUser?.isAdmin != true -> {
                liveData.addRules.postValue(Resource.Error(message = "Bạn không có quyền thêm nội quy"))
                return
            }
        }

        viewModelScope.launch {
            liveData.saveRules.postValue(rulesRepository.saveRules(rules))
        }
    }
}

data class LandlordInfo(
    val phoneNumber: String,
    val email: String,
    val bankName: String? = null,
    val accountNumber: String? = null
)

data class UtilityPrices(
    val electricityPrice: Int,
    val waterPrice: Int
)