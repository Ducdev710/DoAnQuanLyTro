package com.app.motel.data.local

import androidx.room.*
import com.app.motel.data.entity.HopDongEntity
import com.app.motel.data.entity.PhongEntity

@Dao
interface RoomDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(phong: PhongEntity)

    @Update
    suspend fun update(phong: PhongEntity)

    @Delete
    suspend fun delete(phong: PhongEntity)

    @Query("SELECT * FROM Phong WHERE ID = :id")
    suspend fun getPhongById(id: String): PhongEntity?

    @Query("SELECT * FROM Phong")
    suspend fun getAll(): List<PhongEntity>

    @Query("SELECT * FROM Phong WHERE MaKhuTro = :areaId AND (:status IS NULL OR TrangThai = :status)")
    suspend fun getPhongsByKhuTroId(areaId: String, status: String? = null): List<PhongEntity>

    @Query("SELECT * FROM Phong " +
            "LEFT JOIN NguoiThue ON Phong.ID = NguoiThue.MaPhong " +
            "LEFT JOIN HopDong ON Phong.ID = HopDong.MaPhong AND HopDong.HieuLuc = ${HopDongEntity.ACTIVE} " +
            "WHERE NguoiThue.ID = :tenantId AND (:status IS NULL OR Phong.TrangThai = :status)")
    suspend fun getCurrentRoomRentByTenantId(tenantId: String, status: String?): List<PhongEntity>

    @Query("SELECT * FROM Phong WHERE TrangThai = :status OR :status IS NULL")
    suspend fun getPhongsByStatus(status: String?): List<PhongEntity>

    @Query("""
        SELECT * FROM Phong 
        WHERE TrangThai = 'Trống'
        AND MaKhuTro IN (SELECT ID FROM KhuTro WHERE MaChuNha = :landlordId)
    """)
    suspend fun getAvailableRoomsByLandlordId(landlordId: String): List<PhongEntity>

    @Query("UPDATE Phong SET TrangThai = :trangThai WHERE ID = :id")
    suspend fun updateStatus(id: String, trangThai: String)

    @Query("DELETE FROM Phong WHERE MaKhuTro = :id")
    suspend fun deleteByBoardingHouseId(id: String)
}