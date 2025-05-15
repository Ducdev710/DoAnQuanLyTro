package com.app.motel.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.motel.common.AppConstants
import com.app.motel.common.service.DateRoomConverters
import com.app.motel.common.service.StringListRoomConverter
import com.app.motel.data.entity.*

@Database(entities = [
    NguoiDungEntity::class,
    KhuTroEntity::class,
    NguoiThueEntity::class,
    PhongEntity::class,
    HopDongEntity::class,
    DichVuEntity::class,
    HoaDonEntity::class,
    QuyDinhEntity::class,
    KhieuNaiEntity::class,
    ThongBaoEntity::class,
    // VerificationTokenEntity::class - removed
], version = 6, exportSchema = false)
@TypeConverters(StringListRoomConverter::class, DateRoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun boardingHouseDao(): BoardingHouseDAO
    abstract fun userDao(): UserDAO
    abstract fun tenantDao(): TenantDAO
    abstract fun roomDao(): RoomDAO
    abstract fun contractDao(): ContractDAO
    abstract fun serviceDao(): ServiceDAO
    abstract fun billDao(): BillDAO
    abstract fun rulesDAO(): RulesDAO
    abstract fun complaintDao(): ComplaintDAO
    abstract fun notificationDao(): NotificationDAO
    // abstract fun verificationTokenDAO(): VerificationTokenDAO - removed

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Define migration from version 1 to 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the new columns to the NguoiDung table
                database.execSQL("ALTER TABLE NguoiDung ADD COLUMN TenNganHang TEXT")
                database.execSQL("ALTER TABLE NguoiDung ADD COLUMN SoTaiKhoan TEXT")
            }
        }

        // Define migration from version 2 to 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add BiKhoa column to NguoiThue table with default value false (0)
                database.execSQL("ALTER TABLE NguoiThue ADD COLUMN BiKhoa INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Define migration from version 3 to 4
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add MaKhuTro and MaChuNha columns to NguoiThue table
                database.execSQL("ALTER TABLE NguoiThue ADD COLUMN MaKhuTro TEXT")
                database.execSQL("ALTER TABLE NguoiThue ADD COLUMN MaChuNha TEXT")

                // Update existing tenant records with boardinghouse and landlord IDs
                database.execSQL("""
                    UPDATE NguoiThue
                    SET MaKhuTro = (
                        SELECT p.MaKhuTro
                        FROM Phong p
                        WHERE p.ID = NguoiThue.MaPhong
                    ),
                    MaChuNha = (
                        SELECT k.MaChuNha
                        FROM Phong p
                        JOIN KhuTro k ON p.MaKhuTro = k.ID
                        WHERE p.ID = NguoiThue.MaPhong
                    )
                """)
            }
        }

        // Define migration from version 4 to 5
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add LaChuHopDong column to NguoiThue table with default value false (0)
                database.execSQL("ALTER TABLE NguoiThue ADD COLUMN LaChuHopDong INTEGER NOT NULL DEFAULT 0")

                // Set LaChuHopDong=1 for tenants who are currently associated with active contracts
                database.execSQL("""
                    UPDATE NguoiThue
                    SET LaChuHopDong = 1
                    WHERE ID IN (
                        SELECT MaKhach
                        FROM HopDong
                        WHERE HieuLuc = 'Đang hiệu lực'
                    )
                """)
            }
        }

        // Define migration from version 5 to 6
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add isAppliesAllRoom column to DichVu table
                database.execSQL("ALTER TABLE DichVu ADD COLUMN isAppliesAllRoom INTEGER NOT NULL DEFAULT 0")

                // Set isAppliesAllRoom=1 for services without a specific room (global services)
                database.execSQL("""
                    UPDATE DichVu
                    SET isAppliesAllRoom = 1
                    WHERE MaPhong IS NULL
                """)
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                AppConstants.DATABASE_NAME
            )
                //.createFromAsset(AppConstants.DATABASE_FILE_IMPORT)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .fallbackToDestructiveMigration() // Add this line to force recreate the database if schema doesn't match
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d("ROOM", "Room DB created from asset: ${db.path}")
                    }
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.execSQL("PRAGMA foreign_keys=ON;")
                        Log.d("ROOM", "Database opened===>: ${db.path}")
                    }
                })
                .build()
        }
    }
}