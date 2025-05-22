package com.app.motel.data.local

import androidx.room.*
import com.app.motel.data.entity.HoaDonEntity
import com.app.motel.data.entity.HoaDonWithPhong

@Dao
interface BillDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: HoaDonEntity)

    @Update
    suspend fun update(bill: HoaDonEntity)

    @Delete
    suspend fun delete(bill: HoaDonEntity)

    @Query("SELECT * FROM HoaDon WHERE ID = :id")
    suspend fun getById(id: String): HoaDonEntity?

    @Query("SELECT * FROM HoaDon WHERE MaPhong = :roomId ORDER BY Nam DESC, Thang DESC")
    suspend fun getAllByRoom(roomId: String): List<HoaDonEntity>

    @Query("SELECT * FROM HoaDon WHERE MaHopDong = :contractId ORDER BY Nam DESC, Thang DESC")
    suspend fun getAllByContract(contractId: String): List<HoaDonEntity>

    @Query("SELECT * FROM HoaDon WHERE MaPhong = :roomId AND Nam = :year AND Thang = :month")
    suspend fun getByRoomAndMonth(roomId: String, month: Int, year: Int): HoaDonEntity?

    @Query("""
    SELECT *
    FROM HoaDon
    LEFT JOIN Phong ON HoaDon.MaPhong = Phong.ID
    LEFT JOIN KhuTro ON Phong.MaKhuTro = KhuTro.ID
    WHERE KhuTro.ID = :boardingHouseId OR HoaDon.MaPhong IS NULL
""")
    suspend fun getBillByBoardingHouseId(boardingHouseId: String): List<HoaDonWithPhong>

    @Query("""
    SELECT *
    FROM HoaDon
    LEFT JOIN Phong ON HoaDon.MaPhong = Phong.ID
    LEFT JOIN HopDong ON HoaDon.MaHopDong = HopDong.ID
    WHERE HopDong.MaKhach = :tenantId
    ORDER BY Nam DESC, Thang DESC
""")
    suspend fun getBillsByTenantId(tenantId: String): List<HoaDonWithPhong>

    @Query("DELETE FROM HoaDon WHERE MaPhong = :roomId")
    suspend fun deleteByRoomId(roomId: String)

    @Query("DELETE FROM HoaDon WHERE MaHopDong = :contractId")
    suspend fun deleteByContractId(contractId: String)

    @Query("SELECT * FROM HoaDon WHERE maPhong = :roomId")
    suspend fun getBillsByRoomId(roomId: String): List<HoaDonWithPhong>

    @Query("""
    SELECT *
    FROM HoaDon
    LEFT JOIN Phong ON HoaDon.MaPhong = Phong.ID
    LEFT JOIN HopDong ON HoaDon.MaHopDong = HopDong.ID
    LEFT JOIN NguoiThue ON HopDong.MaKhach = NguoiThue.ID
    WHERE NguoiThue.ID = :tenantId
    ORDER BY Nam DESC, Thang DESC
""")
    suspend fun getBillsByTenantRentedRoom(tenantId: String): List<HoaDonWithPhong>

    @Query("""
    UPDATE HoaDon 
    SET MaHopDong = :contractId
    WHERE MaPhong = :roomId AND MaHopDong IS NULL
""")
    suspend fun updateBillsWithContractId(roomId: String, contractId: String)
}