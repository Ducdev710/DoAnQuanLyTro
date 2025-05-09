package com.app.motel.data.model

import com.app.motel.common.service.IDManager
import com.app.motel.data.entity.NguoiThueEntity
import java.util.UUID
import kotlin.random.Random

data class Tenant(
    val id: String,
    val fullName: String,
    val idCard: String? = null,
    val homeTown: String? = null,
    val birthDay: String? = null,
    val phoneNumber: String? = null,
    val status: String? = null,
    val roomId: String? = null,
    val boardingHouseId: String? = null,
    val landlordId: String? = null,
    val username: String,
    val password: String,
    val isLock: Boolean = false,
    val isContractHolder: Boolean = false
){

    @Transient
    var room: Room? = null
    @Transient
                var contract: Contract? = null

                val isRenting: Boolean get() = status == NguoiThueEntity.Status.ACTIVE.value
                val isTemporarilyAbsent: Boolean get() = status == NguoiThueEntity.Status.TEMPORARY_ABSENT.value

                fun toEntity(): NguoiThueEntity{
                    return NguoiThueEntity(
                        id = id,
                        hoTen = fullName,
                        cccd = idCard,
                        queQuan = homeTown,
                        ngaySinh = birthDay,
                        soDienThoai = phoneNumber,
                        trangThai = status,
                        maPhong = roomId,
                        maKhuTro = boardingHouseId,
                        maChuNha = landlordId,
                        tenDangNhap = username,
                        matKhau = password,
                        biKhoa = isLock,
                        laChuHopDong = isContractHolder
                    )
                }

                fun toEntityCreate(): NguoiThueEntity{
                    return NguoiThueEntity(
                        id = IDManager.createID(),
                        hoTen = fullName,
                        cccd = idCard,
                        queQuan = homeTown,
                        ngaySinh = birthDay,
                        soDienThoai = phoneNumber,
                        trangThai = NguoiThueEntity.Status.INACTIVE.value,
                        maPhong = roomId,
                        maKhuTro = boardingHouseId,
                        maChuNha = landlordId,
                        tenDangNhap = username,
                        matKhau = password,
                        biKhoa = isLock,
                        laChuHopDong = isContractHolder
                    )
                }
            }