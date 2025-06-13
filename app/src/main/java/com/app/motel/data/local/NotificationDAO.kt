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

    @Query("SELECT ThongBao.* FROM ThongBao " +
            "LEFT JOIN Phong ON (ThongBao.MaPhong = Phong.ID OR ThongBao.MaPhong IS NULL) AND ThongBao.MaKhuTro = Phong.MaKhuTro " +
            "LEFT JOIN NguoiThue ON Phong.ID = NguoiThue.MaPhong " +
            "JOIN HopDong ON Phong.ID = HopDong.MaPhong AND HopDong.HieuLuc = ${HopDongEntity.ACTIVE} " +
            "WHERE NguoiThue.ID = :tenantId " +
            "ORDER BY ThongBao.NgayTao DESC")
    suspend fun getUserNotification(tenantId: String): List<ThongBaoEntity>

    @Query("UPDATE ThongBao SET DaDoc = 1 WHERE ID = :id")
    suspend fun markAsRead(id: String)

    @Query("DELETE FROM ThongBao WHERE MaKhuTro = :id")
    suspend fun deleteByBoardingHouseId(id: String)

    // Đếm số thông báo chưa đọc cho người thuê (bao gồm thông báo chung và riêng)
    @Query("SELECT COUNT(*) FROM ThongBao " +
            "LEFT JOIN Phong ON (ThongBao.MaPhong = Phong.ID OR ThongBao.MaPhong IS NULL) AND ThongBao.MaKhuTro = Phong.MaKhuTro " +
            "LEFT JOIN NguoiThue ON Phong.ID = NguoiThue.MaPhong " +
            "JOIN HopDong ON Phong.ID = HopDong.MaPhong AND HopDong.HieuLuc = ${HopDongEntity.ACTIVE} " +
            "WHERE NguoiThue.ID = :tenantId AND ThongBao.DaDoc = 0")
    suspend fun countUnreadNotificationsForTenant(tenantId: String): Int

    // Đánh dấu tất cả thông báo là đã đọc cho người thuê
    @Query("UPDATE ThongBao SET DaDoc = 1 " +
            "WHERE ID IN (SELECT ThongBao.ID FROM ThongBao " +
            "LEFT JOIN Phong ON (ThongBao.MaPhong = Phong.ID OR ThongBao.MaPhong IS NULL) AND ThongBao.MaKhuTro = Phong.MaKhuTro " +
            "LEFT JOIN NguoiThue ON Phong.ID = NguoiThue.MaPhong " +
            "JOIN HopDong ON Phong.ID = HopDong.MaPhong AND HopDong.HieuLuc = ${HopDongEntity.ACTIVE} " +
            "WHERE NguoiThue.ID = :tenantId AND ThongBao.DaDoc = 0)")
    suspend fun markAllNotificationsAsReadForTenant(tenantId: String): Int

    // Đếm số thông báo chung chưa đọc cho người thuê
    @Query("SELECT COUNT(*) FROM ThongBao " +
            "LEFT JOIN Phong ON ThongBao.MaPhong IS NULL AND ThongBao.MaKhuTro = Phong.MaKhuTro " +
            "LEFT JOIN NguoiThue ON Phong.ID = NguoiThue.MaPhong " +
            "JOIN HopDong ON Phong.ID = HopDong.MaPhong AND HopDong.HieuLuc = ${HopDongEntity.ACTIVE} " +
            "WHERE NguoiThue.ID = :tenantId AND ThongBao.DaDoc = 0 AND ThongBao.MaPhong IS NULL")
    suspend fun countUnreadGeneralNotificationsForTenant(tenantId: String): Int

    // Đếm số thông báo riêng chưa đọc cho người thuê
    @Query("SELECT COUNT(*) FROM ThongBao " +
            "LEFT JOIN Phong ON ThongBao.MaPhong = Phong.ID " +
            "LEFT JOIN NguoiThue ON Phong.ID = NguoiThue.MaPhong " +
            "JOIN HopDong ON Phong.ID = HopDong.MaPhong AND HopDong.HieuLuc = ${HopDongEntity.ACTIVE} " +
            "WHERE NguoiThue.ID = :tenantId AND ThongBao.DaDoc = 0 AND ThongBao.MaPhong IS NOT NULL")
    suspend fun countUnreadRoomNotificationsForTenant(tenantId: String): Int
}