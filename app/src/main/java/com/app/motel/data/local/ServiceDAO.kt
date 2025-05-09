package com.app.motel.data.local

import androidx.room.*
import com.app.motel.data.entity.DichVuEntity
import com.app.motel.data.entity.PhongEntity

@Dao
interface ServiceDAO {

    @Query("SELECT * FROM DichVu")
    suspend fun getAllServices(): List<DichVuEntity>

    @Query("SELECT * FROM DichVu WHERE ID = :id")
    suspend fun getServiceById(id: String): DichVuEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(service: DichVuEntity)

    @Update
    suspend fun update(service: DichVuEntity)

    @Delete
    suspend fun delete(service: DichVuEntity)

    @Query("SELECT * FROM DichVu WHERE MaKhuTro = :areaId")
    suspend fun getServiceByKhuTroId(areaId: String): List<DichVuEntity>

    @Query("""
        SELECT * FROM DichVu 
        WHERE MaKhuTro = :maKhuTro 
        AND (MaPhong IS NULL OR MaPhong = :maPhong)
    """)
    suspend fun getServiceOfRoom(maKhuTro: String, maPhong: String): List<DichVuEntity>

    @Query("DELETE FROM DichVu WHERE MaKhuTro = :id")
    suspend fun deleteByBoardingHouseId(id: String)
}
