package com.app.motel.data.local

import androidx.room.*
import com.app.motel.data.entity.KhieuNaiEntity

@Dao
interface ComplaintDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: KhieuNaiEntity)

    @Update
    suspend fun updateComplaint(complaint: KhieuNaiEntity)

    @Delete
    suspend fun deleteComplaint(complaint: KhieuNaiEntity)

    @Query("SELECT * FROM KhieuNai WHERE ID = :id")
    suspend fun getComplaintById(id: String): KhieuNaiEntity?

    @Query("SELECT * FROM KhieuNai ORDER BY NgayTao DESC")
    suspend fun getAllComplaints(): List<KhieuNaiEntity>

    @Query("SELECT * FROM KhieuNai WHERE MaPhong = :roomId")
    suspend fun getComplaintsByRoom(roomId: String): List<KhieuNaiEntity>

    @Query("SELECT * FROM KhieuNai WHERE NguoiNop = :userId")
    suspend fun getComplaintsByUser(userId: String): List<KhieuNaiEntity>

    @Query("DELETE FROM KhieuNai WHERE MaPhong = :roomId")
    suspend fun deleteByRoomId(roomId: String)

    @Query("SELECT * FROM KhieuNai " +
            "LEFT JOIN Phong ON KhieuNai.MaPhong = Phong.ID " +
            "LEFT JOIN KhuTro ON Phong.MaKhuTro = KhuTro.ID " +
            "WHERE Phong.MaKhuTro = :boardingHouseId" +
            "" )
    suspend fun getByBoardingHouseId(boardingHouseId: String): List<KhieuNaiEntity>

    @Query("SELECT * FROM KhieuNai WHERE NguoiNop = :userId ORDER BY NgayTao DESC")
    suspend fun getByTenantId(userId: String): List<KhieuNaiEntity>

    @Query("UPDATE KhieuNai SET TrangThai = :state WHERE ID = :id")
    suspend fun updateStateComplaint(id: String, state: String)
}
