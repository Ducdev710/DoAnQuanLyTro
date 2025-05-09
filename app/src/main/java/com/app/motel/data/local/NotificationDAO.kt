package com.app.motel.data.local

import androidx.room.*
import com.app.motel.data.entity.HopDongEntity
import com.app.motel.data.entity.ThongBaoEntity

@Dao
interface NotificationDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: ThongBaoEntity)

    @Update
    suspend fun update(notification: ThongBaoEntity)

    @Delete
    suspend fun delete(notification: ThongBaoEntity)

    @Query("SELECT * FROM ThongBao WHERE ID = :id")
    suspend fun getById(id: String): ThongBaoEntity?

    @Query("SELECT * FROM ThongBao ORDER BY NgayTao DESC")
    suspend fun getAll(): List<ThongBaoEntity>

    @Query("SELECT * FROM ThongBao WHERE MaKhuTro = :khuTroId")
    suspend fun getByKhuTro(khuTroId: String): List<ThongBaoEntity>

    @Query("SELECT * FROM ThongBao WHERE MaPhong = :phongId")
    suspend fun getByPhong(phongId: String): List<ThongBaoEntity>

    @Query("SELECT * FROM ThongBao LEFT JOIN KhuTro ON ThongBao.MaKhuTro = KhuTro.ID WHERE KhuTro.ID = :boardingHouseId ORDER BY NgayTao DESC")
    suspend fun getAdminNotification(boardingHouseId: String): List<ThongBaoEntity>


//    @Query("SELECT * FROM ThongBao " +
//            "WHERE ThongBao.MaPhong IS NULL " +
//            "   OR ThongBao.MaPhong IN ( SELECT MaPhong FROM HopDong " +
//            "       WHERE HieuLuc = ${HopDongEntity.ACTIVE} AND MaKhach = :tenantId )")
    @Query("SELECT * FROM ThongBao " +
            "LEFT JOIN Phong ON (ThongBao.MaPhong = Phong.ID OR ThongBao.MaPhong IS NULL) AND ThongBao.MaKhuTro = Phong.MaKhuTro " +
            "LEFT JOIN NguoiThue ON Phong.ID = NguoiThue.MaPhong " +
            " JOIN HopDong ON Phong.ID = HopDong.MaPhong AND HopDong.HieuLuc = ${HopDongEntity.ACTIVE} " +
            "WHERE NguoiThue.ID = :tenantId")
    suspend fun getUserNotification(tenantId: String): List<ThongBaoEntity>


    @Query("UPDATE ThongBao SET DaDoc = 1 WHERE ID = :id")
    suspend fun markAsRead(id: String)

    @Query("DELETE FROM ThongBao WHERE MaKhuTro = :id")
    suspend fun deleteByBoardingHouseId(id: String)
}
