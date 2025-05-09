package com.app.motel.data.repository

import com.app.motel.common.service.IDManager
import com.app.motel.data.entity.NguoiThueEntity
import com.app.motel.data.local.BoardingHouseDAO
import com.app.motel.data.local.RoomDAO
import com.app.motel.data.local.TenantDAO
import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.Room
import com.app.motel.data.network.ApiMock
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.random.Random

class HomeRepository @Inject constructor(
    private val api: ApiMock,
    private val roomDAO: RoomDAO,
    private val boardingHouseDAO: BoardingHouseDAO,
    private val tenantDAO: TenantDAO,
) {

    suspend fun getRoomsNetwork() : List<Room> = api.getMotel()

    suspend fun getBoardingByUserId(userId: String): List<BoardingHouse> {
        return boardingHouseDAO.getByUserId(userId).map { it.toModel() }
    }
}