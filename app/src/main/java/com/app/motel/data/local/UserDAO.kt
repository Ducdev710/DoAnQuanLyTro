package com.app.motel.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.motel.data.entity.NguoiDungEntity

@Dao
interface UserDAO {

    @Query("SELECT * FROM NguoiDung")
    suspend fun getAll(): List<NguoiDungEntity>

    @Query("SELECT * FROM NguoiDung WHERE ID = :id")
    suspend fun getById(id: String): NguoiDungEntity?

    @Query("SELECT * FROM NguoiDung WHERE TenDangNhap = :username")
    suspend fun getByUsername(username: String): NguoiDungEntity?

    @Update
    suspend fun update(user: NguoiDungEntity)

    @Delete
    suspend fun delete(user: NguoiDungEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: NguoiDungEntity)

    @Query("SELECT * FROM NguoiDung WHERE Email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): NguoiDungEntity?

}
