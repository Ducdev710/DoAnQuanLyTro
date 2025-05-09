package com.app.motel.data.local

import androidx.room.*
import com.app.motel.data.entity.HopDongEntity

@Dao
interface ContractDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contract: HopDongEntity)

    @Update
    suspend fun update(contract: HopDongEntity)

    @Delete
    suspend fun delete(contract: HopDongEntity)

    @Query("SELECT * FROM HopDong WHERE ID = :id")
    suspend fun getById(id: String): HopDongEntity?

    @Query("SELECT * FROM HopDong")
    suspend fun getAll(): List<HopDongEntity>

    // Changed this query to only get contracts where the tenant is the direct contract holder
    @Query("SELECT * FROM HopDong WHERE MaKhach = :customerId ORDER BY HieuLuc DESC")
    suspend fun getByTenantId(customerId: String): List<HopDongEntity>

    @Query("SELECT * FROM HopDong WHERE MaPhong = :roomId")
    suspend fun getByRoomId(roomId: String): List<HopDongEntity>

    @Query("SELECT * FROM HopDong WHERE MaPhong = :roomId AND MaKhach = :tenantId")
    suspend fun getByRoomTenantId(roomId: String, tenantId: String): List<HopDongEntity>

    @Query("""
    SELECT *
    FROM HopDong
    LEFT JOIN Phong ON HopDong.MaPhong = Phong.ID
    LEFT JOIN KhuTro ON Phong.MaKhuTro = KhuTro.ID
    WHERE KhuTro.ID = :boardingHouseId OR HopDong.MaPhong IS NULL
""")
    suspend fun getContractsByBoardingId(boardingHouseId: String): List<HopDongEntity>

    @Query("UPDATE HopDong SET MaPhong = NULL WHERE MaPhong = :roomId")
    suspend fun updateRoomIdToNull(roomId: String)

    @Query("DELETE FROM HopDong WHERE MaPhong = :roomId")
    suspend fun deleteByRoomId(roomId: String)
}