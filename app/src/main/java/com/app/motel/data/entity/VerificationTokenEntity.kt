//package com.app.motel.data.entity
//
//import androidx.room.ColumnInfo
//import androidx.room.Entity
//import androidx.room.ForeignKey
//import androidx.room.Index
//import androidx.room.PrimaryKey
//
//@Entity(
//    tableName = "verification_tokens",
//    foreignKeys = [
//        ForeignKey(
//            entity = NguoiDungEntity::class,
//            parentColumns = ["ID"],
//            childColumns = ["user_id"],
//            onDelete = ForeignKey.CASCADE,
//            onUpdate = ForeignKey.CASCADE
//        )
//    ],
//    indices = [Index("user_id")]
//)
//data class VerificationTokenEntity(
//    @PrimaryKey
//    val token: String,
//
//    @ColumnInfo(name = "user_id")
//    val userId: String,
//
//    @ColumnInfo(name = "expiry_date")
//    val expiryDate: Long,
//
//    @ColumnInfo(name = "is_used")
//    val isUsed: Boolean
//)