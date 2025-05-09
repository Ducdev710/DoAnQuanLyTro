package com.app.motel.data.local

import androidx.room.*
import com.app.motel.data.entity.QuyDinhEntity

@Dao
interface RulesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(regulation: QuyDinhEntity)

    @Update
    suspend fun update(regulation: QuyDinhEntity)

    @Delete
    suspend fun delete(regulation: QuyDinhEntity)

    @Query("SELECT * FROM QuyDinh WHERE ID = :id")
    suspend fun getRegulationById(id: String): QuyDinhEntity?

    @Query("SELECT * FROM QuyDinh ORDER BY NgayTao DESC")
    suspend fun getAllRegulations(): List<QuyDinhEntity>

    @Query("SELECT * FROM QuyDinh WHERE MaKhuTro = :khuTroId")
    suspend fun getRegulationsByKhuTro(khuTroId: String): List<QuyDinhEntity>

    @Query("DELETE FROM QuyDinh WHERE MaKhuTro = :id")
    suspend fun deleteByBoardingHouseId(id: String)
}
