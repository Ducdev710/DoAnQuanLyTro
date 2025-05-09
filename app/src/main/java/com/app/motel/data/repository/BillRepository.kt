package com.app.motel.data.repository

import android.util.Log
import com.app.motel.common.service.DateConverter
import com.app.motel.common.service.DateConverter.toCalendar
import com.app.motel.data.entity.HoaDonWithPhong
import com.app.motel.data.local.BillDAO
import com.app.motel.data.local.BoardingHouseDAO
import com.app.motel.data.local.ContractDAO
import com.app.motel.data.local.RoomDAO
import com.app.motel.data.model.Bill
import com.app.motel.data.model.Resource
import java.util.Calendar
import javax.inject.Inject

class BillRepository @Inject constructor(
    private val boardingHouseDAO: BoardingHouseDAO,
    private val roomDAO: RoomDAO,
    private val billDAO: BillDAO,
    private val contractDAO: ContractDAO,
) {
    companion object{
        private const val SEARCH_MONTH_MAX_LENGTH = 36 // 3 year
    }

    suspend fun getBillByBoardingHouseId(boardingHouseId: String): List<Bill> {
        val hoaDonEntity: List<HoaDonWithPhong> = billDAO.getBillByBoardingHouseId(boardingHouseId)
        return hoaDonEntity.map { billWithRoom ->
            val room = billWithRoom.phong?.toModel()
            billWithRoom.hoaDon.toModel().apply {
                this.room = room
            }
        }
    }

    suspend fun getBillByTenantRentedRoom(tenantId: String): List<Bill> {
        try {
            val contractEntities = contractDAO.getByTenantId(tenantId)
            val bills: ArrayList<Bill> = arrayListOf()

            contractEntities.forEach { contractEntity ->
                var leftDate = DateConverter.localStringToDate(contractEntity.ngayBatDau)?.toCalendar()
                val rightDate = DateConverter.localStringToDate(contractEntity.ngayKetThuc)?.toCalendar()
                if(leftDate != null && rightDate != null){
                    while (leftDate!!.before(rightDate)){
                        Log.e("getBillByTenantRentedRoom", "leftDate ${leftDate.time}")
                        val bill = billDAO.getByRoomAndMonth(
                            contractEntity.maPhong ?: "",
                            leftDate.get(Calendar.MONTH),
                            leftDate.get(Calendar.YEAR),
                        )
                        if(bill != null){
                            bills.add(bill.toModel().apply {
                                this.room = contractEntity.maPhong?.let { roomDAO.getPhongById(it)?.toModel() }
                            })
                        }

                        leftDate = DateConverter.calculateMonth(leftDate.time, 1)
                    }
                }
            }
            Log.e("getBillByTenantRentedRoom", "bills ok $bills")
            return bills.reversed()
        }catch (e: Exception){
            Log.e("getBillByTenantRentedRoom", "bills error $e")
            return listOf()
        }
    }

    suspend fun checkBillCreateDate(roomId: String, createdDate: Calendar): Resource<Bill> {
        return try {

            for (currentMonthSearchIndex in 0 .. SEARCH_MONTH_MAX_LENGTH) {
                val forwardDate = DateConverter.calculateMonth(
                    createdDate.time,
                    currentMonthSearchIndex)

                val bill = billDAO.getByRoomAndMonth(
                    roomId,
                    forwardDate.get(Calendar.MONTH) + 1,
                    forwardDate.get(Calendar.YEAR),
                )

                if (bill != null) {
                    return Resource.Error(message = "Hóa đơn đã cũ, hãy tạo đơn gần nhất")
                }
            }
            Resource.Success(data = null)
        } catch (e: Exception) {
            Resource.Error(message = e.toString())
        }
    }

    suspend fun getPreviousBill(roomId: String): Resource<Bill> {
        return try {
            for (currentMonthSearchIndex in 0 .. SEARCH_MONTH_MAX_LENGTH) {
                val previousDate = DateConverter.calculateMonth(
                    DateConverter.getCurrentDateTime(),
                    -currentMonthSearchIndex)

                val bill = billDAO.getByRoomAndMonth(
                    roomId,
                    previousDate.get(Calendar.MONTH) + 1,
                    previousDate.get(Calendar.YEAR),
                )
                Log.e("getPreviousBill", "bill $${previousDate.time} $bill")
                if (bill != null) {
                    return Resource.Success(bill.toModel())
                }
            }
            Resource.Success(data = null, message = "Không tìm thấy hóa đơn")
        } catch (e: Exception) {
            Resource.Error(message = e.toString())
        }
    }

    suspend fun createBill(bill: Bill): Resource<Bill>{
        return try {
            val billEntity = bill.toCreateEntity()
            billDAO.insert(billEntity)
            Resource.Success(billEntity.toModel())
        }catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }

    suspend fun updateBill(bill: Bill): Resource<Bill>{
        return try {
            val billEntity = bill.toEntity()
            billDAO.update(billEntity)
            Resource.Success(billEntity.toModel())
        }catch (e: Exception){
            Resource.Error(message = e.toString())
        }
    }
}