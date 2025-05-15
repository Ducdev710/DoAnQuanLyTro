//package com.app.motel.data.local
//
//import androidx.room.*
//import com.app.motel.data.entity.VerificationTokenEntity
//
//@Dao
//interface VerificationTokenDAO {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertToken(token: VerificationTokenEntity)
//
//    @Query("SELECT * FROM verification_tokens WHERE token = :token")
//    fun getByToken(token: String): VerificationTokenEntity?
//
//    @Query("UPDATE verification_tokens SET is_used = 1 WHERE token = :token")
//    fun markTokenAsUsed(token: String)
//
//    @Query("DELETE FROM verification_tokens WHERE user_id = :userId AND is_used = 0")
//    fun deleteUnusedTokensByUserId(userId: String)
//
//    @Query("DELETE FROM verification_tokens WHERE expiry_date < :currentTime")
//    fun deleteExpiredTokens(currentTime: Long)
//}