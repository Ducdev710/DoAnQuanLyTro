package com.app.motel.data.model

import com.app.motel.common.service.DateConverter
import com.app.motel.common.service.IDManager
import com.app.motel.data.entity.HopDongEntity
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class Contract(
    val id: String = "",
    val name: String?,
    val duration: Int? = null,
    val createdDate: String? = null,
    val startDate: String?,
    val endDate: String?,
    val deposit: String?,
    val status: Int? = null,
    val isActive: Int? = null,
    val roomId: String? = null,
    val customerId: String? = null,
    val note: String?,
    val terminationReason: String? = null,
    val refundAmount: String? = null,
    val deductionReason: String? = null
) {
    var room: Room? = null
    var tenant: Tenant? = null

    val state: State
        get() = State.getStateByDate(endDate ?: "")

    val isNearEnd get () = state == State.NEAR_END

    fun toEntity() = HopDongEntity(
        id = id,
        ten = name,
        thoiHan = duration,
        ngayLapHopDong = createdDate,
        ngayBatDau = startDate,
        ngayKetThuc = endDate,
        tienCoc = deposit,
        trangThai = status,
        hieuLuc = isActive,
        maPhong = roomId,
        maKhach = customerId,
        ghiChu = note,
        lyDoKetThuc = terminationReason,
        soTienHoanTra = refundAmount,
        lyDoKhauTru = deductionReason
    )

    fun toCreateEntity() = HopDongEntity(
        id = IDManager.createID(),
        ten = name,
        thoiHan = DateConverter.monthsBetweenDates(DateConverter.localStringToDate(startDate ?: "")!!, DateConverter.localStringToDate(endDate ?: "")!!),
        ngayLapHopDong = createdDate,
        ngayBatDau = startDate,
        ngayKetThuc = endDate,
        tienCoc = deposit,
        trangThai = HopDongEntity.STATE_ACTIVE,
        hieuLuc = HopDongEntity.ACTIVE,
        maPhong = roomId,
        maKhach = customerId,
        ghiChu = note,
        lyDoKetThuc = null,
        soTienHoanTra = null,
        lyDoKhauTru = null
    )

    enum class State(
        val value: String
    ) {
        ACTIVE("Còn hạn"),
        NEAR_END("Sắp hết hạn"),
        ENDED("Đã hết hạn"),
        UNKNOWN("Không xác định");

        companion object {
            fun getStateByValue(value: String): State{
                return entries.firstOrNull{ it.value == value } ?: ACTIVE
            }

            fun getStateByDate(date: String): State {
                val expiryDate = DateConverter.localStringToDate(date)
                val today = DateConverter.getCurrentDateTime()

                if (expiryDate == null) return UNKNOWN

                val calToday = Calendar.getInstance()
                val calExpiry = Calendar.getInstance()
                calToday.time = today
                calExpiry.time = expiryDate

                return when {
                    calExpiry.before(calToday) -> ENDED
                    getDaysBetween(calToday, calExpiry) <= 30 -> NEAR_END
                    else -> ACTIVE
                }
            }

            private fun getDaysBetween(start: Calendar, end: Calendar): Long {
                val diffMillis = end.timeInMillis - start.timeInMillis
                return TimeUnit.MILLISECONDS.toDays(diffMillis)
            }
        }
    }
}