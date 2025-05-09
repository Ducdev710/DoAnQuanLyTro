package com.app.motel.data.repository

import com.app.motel.data.local.BoardingHouseDAO
import com.app.motel.data.local.RulesDAO
import com.app.motel.data.local.UserDAO
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Rules
import com.app.motel.feature.rules.viewmodel.LandlordInfo
import javax.inject.Inject

class RulesRepository @Inject constructor(
    private val rulesDAO: RulesDAO,
    private val boardingHouseDAO: BoardingHouseDAO,
    private val userDAO: UserDAO, // Add UserDAO dependency
){
    suspend fun getRulesByBoardingHouseId(boardingHouseId: String): List<Rules> {
        val rulesEntities = rulesDAO.getRegulationsByKhuTro(boardingHouseId)
        return rulesEntities.map { it.toModel() } as ArrayList<Rules>
    }

    suspend fun getRulesByTenantId(tenantId: String): List<Rules> {
        val boardingHouseEntities = boardingHouseDAO.getByTenantId(tenantId)

        val rules = boardingHouseEntities.flatMap { boardingHouseEntity ->
            getRulesByBoardingHouseId(boardingHouseEntity.id)
        }
        return rules
    }

    suspend fun getLandlordInfoByBoardingHouseId(boardingHouseId: String): LandlordInfo? {
        // Get the landlord ID for this boarding house
        val boardingHouse = boardingHouseDAO.getById(boardingHouseId) ?: return null
        val landlordId = boardingHouse.maChuNha ?: return null

        // Get the user data for this landlord
        val landlord = userDAO.getById(landlordId) ?: return null

        // Map user data to LandlordInfo
        return LandlordInfo(
            phoneNumber = landlord.soDienThoai ?: "",
            email = landlord.email ?: "",
            bankName = landlord.tenNganHang,
            accountNumber = landlord.soTaiKhoan
        )
    }

    suspend fun getLandlordIdForTenant(tenantId: String): String? {
        // Find boarding houses where this tenant lives
        val boardingHouses = boardingHouseDAO.getByTenantId(tenantId)
        if (boardingHouses.isEmpty()) return null

        // Return the admin ID of the first boarding house (landlord)
        return boardingHouses.firstOrNull()?.maChuNha
    }

    suspend fun getUserById(userId: String): UserInfo? {
        val user = userDAO.getById(userId) ?: return null

        return UserInfo(
            phoneNumber = user.soDienThoai,
            email = user.email,
            bankName = user.tenNganHang,
            accountNumber = user.soTaiKhoan
        )
    }

    suspend fun saveRules(rules: List<Rules>): Resource<Boolean>{
        return try {
            rules.forEach {
                rulesDAO.insert(it.toEntity())
            }
            Resource.Success(true)
        }catch (e: Exception){
            Resource.Error(message = e.message ?: "Unknown error")
        }
    }
}

/**
 * Simple data class to represent user information for landlord contact details
 */
data class UserInfo(
    val phoneNumber: String? = null,
    val email: String? = null,
    val bankName: String? = null,
    val accountNumber: String? = null
)