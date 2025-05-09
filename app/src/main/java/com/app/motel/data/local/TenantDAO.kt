package com.app.motel.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.motel.data.entity.NguoiThueEntity

@Dao
interface TenantDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nguoiThue: NguoiThueEntity)

    @Update
    suspend fun update(nguoiThue: NguoiThueEntity)

    @Delete
    suspend fun delete(nguoiThue: NguoiThueEntity)

    @Query("SELECT * FROM NguoiThue WHERE ID = :id")
    suspend fun getNguoiThueById(id: String): NguoiThueEntity?

    @Query("SELECT * FROM NguoiThue WHERE ID = :tenantId")
    suspend fun getTenantById(tenantId: String): NguoiThueEntity?

    @Query("SELECT * FROM NguoiThue")
    suspend fun getTenants(): List<NguoiThueEntity>

    @Query("SELECT * FROM NguoiThue WHERE MaChuNha = :landlordId")
    suspend fun getTenantsByLandlordId(landlordId: String): List<NguoiThueEntity>

    @Query("""
    SELECT * FROM NguoiThue 
    WHERE (:roomId IS NULL AND (MaPhong IS NULL OR MaPhong = '')) 
       OR (MaPhong = :roomId)
""")
    suspend fun getTenantByRoomId(roomId: String?): List<NguoiThueEntity>

    @Query("SELECT * FROM NguoiThue WHERE TenDangNhap = :username AND MatKhau = :password")
    suspend fun getNguoiThueByUsernameAndPassword(username: String, password: String): NguoiThueEntity?

    @Query("SELECT * FROM NguoiThue WHERE TenDangNhap = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): NguoiThueEntity?

    @Query("SELECT * FROM NguoiThue WHERE ID = :id")
    suspend fun getById(id: String): NguoiThueEntity?

    @Query("UPDATE NguoiThue SET MaPhong = :roomId, TrangThai = :status WHERE ID = :id")
    suspend fun updateRent(id: String, roomId: String?, status: String)

    @Query("UPDATE NguoiThue SET MaPhong = :roomId WHERE ID = :tenantId")
    suspend fun updateTenantRoom(tenantId: String, roomId: String?)

    @Query("UPDATE NguoiThue SET MaPhong = NULL, TrangThai = :status WHERE MaPhong = :roomId")
    suspend fun updateRentByRoomId(roomId: String, status: String = NguoiThueEntity.Status.INACTIVE.value)

    @Query("DELETE FROM NguoiThue WHERE ID = :tenantId")
    suspend fun deleteTenantById(tenantId: String)

    @Query("UPDATE NguoiThue SET BiKhoa = :lockStatus WHERE ID = :id")
    suspend fun updateLockStatus(id: String, lockStatus: Boolean)

    @Query("SELECT * FROM NguoiThue WHERE TrangThai = :status AND BiKhoa = :locked")
    suspend fun getTenantsByStatusAndLock(status: String, locked: Boolean): List<NguoiThueEntity>

    @Query("SELECT * FROM NguoiThue WHERE TrangThai = :status OR BiKhoa = :locked")
    suspend fun getTenantsByStatusOrLock(status: String, locked: Boolean): List<NguoiThueEntity>

    @Query("SELECT * FROM NguoiThue WHERE TrangThai = :status AND BiKhoa = 0")
    suspend fun getActiveTenants(status: String): List<NguoiThueEntity>

    @Query("SELECT * FROM NguoiThue WHERE TrangThai = :status OR BiKhoa = 1")
    suspend fun getInactiveTenants(status: String): List<NguoiThueEntity>

    @Query("UPDATE NguoiThue SET TrangThai = :status WHERE ID = :tenantId")
    suspend fun updateTenantStatus(tenantId: String, status: String)

    @Query("UPDATE NguoiThue SET LaChuHopDong = :isContractHolder WHERE ID = :tenantId")
    suspend fun updateIsContractHolder(tenantId: String, isContractHolder: Boolean)

    @Query("UPDATE NguoiThue SET MaPhong = :roomId WHERE ID = :tenantId")
    suspend fun updateRentedRoom(tenantId: String, roomId: String)

    @Query("UPDATE NguoiThue SET TrangThai = :status, LaChuHopDong = :isContractHolder WHERE ID = :tenantId")
    suspend fun updateStatusAndContractHolder(tenantId: String, status: String, isContractHolder: Boolean)

    @Query("SELECT * FROM NguoiThue WHERE MaKhuTro = :boardingHouseId")
    suspend fun getTenantsByBoardingHouseId(boardingHouseId: String): List<NguoiThueEntity>

    @Query("SELECT * FROM NguoiThue WHERE MaChuNha = :landlordId AND MaKhuTro = :boardingHouseId")
    suspend fun getTenantsByLandlordAndBoardingHouse(landlordId: String, boardingHouseId: String): List<NguoiThueEntity>
}