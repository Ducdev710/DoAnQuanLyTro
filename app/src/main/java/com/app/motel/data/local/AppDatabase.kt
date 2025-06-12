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
], version = 13, exportSchema = false)
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

        // Define migration from version 6 to 7 for contract termination fields
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add contract termination columns to HopDong table
                database.execSQL("ALTER TABLE HopDong ADD COLUMN LyDoKetThuc TEXT")
                database.execSQL("ALTER TABLE HopDong ADD COLUMN SoTienHoanTra TEXT")
                database.execSQL("ALTER TABLE HopDong ADD COLUMN LyDoKhauTru TEXT")
            }
        }

        // Define migration from version 7 to 8 for previous meter readings
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add previous meter readings columns to HoaDon table
                database.execSQL("ALTER TABLE HoaDon ADD COLUMN SoDienCu INTEGER")
                database.execSQL("ALTER TABLE HoaDon ADD COLUMN SoNuocCu INTEGER")

                // Update existing bills with previous readings based on older bills
                database.execSQL("""
                    UPDATE HoaDon AS current
                    SET 
                        SoDienCu = (
                            SELECT prev.SODIEN
                            FROM HoaDon AS prev
                            WHERE 
                                prev.MaPhong = current.MaPhong AND
                                ((prev.Nam < current.Nam) OR 
                                (prev.Nam = current.Nam AND prev.Thang < current.Thang))
                            ORDER BY prev.Nam DESC, prev.Thang DESC
                            LIMIT 1
                        ),
                        SoNuocCu = (
                            SELECT prev.SONUOC
                            FROM HoaDon AS prev
                            WHERE 
                                prev.MaPhong = current.MaPhong AND
                                ((prev.Nam < current.Nam) OR 
                                (prev.Nam = current.Nam AND prev.Thang < current.Thang))
                            ORDER BY prev.Nam DESC, prev.Thang DESC
                            LIMIT 1
                        )
                """)

                // Set null values to 0 for previous readings
                database.execSQL("UPDATE HoaDon SET SoDienCu = 0 WHERE SoDienCu IS NULL")
                database.execSQL("UPDATE HoaDon SET SoNuocCu = 0 WHERE SoNuocCu IS NULL")
            }
        }

        // Define migration from version 8 to 9 for additional fee
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add additional fee column (PhuPhi) to HoaDon table
                database.execSQL("ALTER TABLE HoaDon ADD COLUMN PhuPhi TEXT DEFAULT '0'")
            }
        }

        // Define migration from version 9 to 10 for room notes
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add notes column (GhiChu) to Phong table
                database.execSQL("ALTER TABLE Phong ADD COLUMN GhiChu TEXT")
            }
        }

        // Define migration from version 10 to 11 for repair notes
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add repair notes column (GhiChuSuaChua) to Phong table
                database.execSQL("ALTER TABLE Phong ADD COLUMN GhiChuSuaChua TEXT")
            }
        }

        // Define migration from version 11 to 12 for bill contract ID
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add contract ID column (MaHopDong) to HoaDon table
                database.execSQL("ALTER TABLE HoaDon ADD COLUMN MaHopDong TEXT")

                // Update existing bills with contract IDs from active contracts
                database.execSQL("""
            UPDATE HoaDon
            SET MaHopDong = (
                SELECT hd.ID
                FROM HopDong hd
                WHERE hd.MaPhong = HoaDon.MaPhong
                AND hd.HieuLuc = 'Đang hiệu lực'
                LIMIT 1
            )
        """)

                // For older bills, try to assign contracts based on date
                database.execSQL("""
            UPDATE HoaDon
            SET MaHopDong = (
                SELECT hd.ID
                FROM HopDong hd
                WHERE hd.MaPhong = HoaDon.MaPhong
                AND (HoaDon.Nam * 100 + HoaDon.Thang) BETWEEN 
                    (strftime('%Y', hd.NgayBatDau) * 100 + strftime('%m', hd.NgayBatDau)) 
                    AND 
                    CASE 
                        WHEN hd.NgayKetThuc IS NULL THEN 999999
                        ELSE (strftime('%Y', hd.NgayKetThuc) * 100 + strftime('%m', hd.NgayKetThuc))
                    END
                LIMIT 1
            )
            WHERE MaHopDong IS NULL
        """)

                // Add foreign key relationship between HoaDon and HopDong tables
                database.execSQL("""
            CREATE TABLE HoaDon_new (
                ID TEXT NOT NULL PRIMARY KEY,
                Ten TEXT,
                MaPhong TEXT,
                MaHopDong TEXT,
                NgayTao TEXT,
                Thang INTEGER NOT NULL,
                Nam INTEGER NOT NULL,
                GiaThue REAL NOT NULL,
                SODIEN INTEGER,
                SONUOC INTEGER,
                SoDienTieuThu INTEGER,
                SoNuocTieuThu INTEGER,
                GiaDichVu TEXT,
                TienMienGiam TEXT,
                TongTien TEXT,
                TrangThai INTEGER,
                GhiChu TEXT,
                SoDienCu INTEGER,
                SoNuocCu INTEGER,
                PhuPhi TEXT DEFAULT '0',
                FOREIGN KEY (MaPhong) REFERENCES Phong(ID) ON DELETE SET NULL,
                FOREIGN KEY (MaHopDong) REFERENCES HopDong(ID) ON DELETE SET NULL
            )
        """)

                // Copy data from old table to new table
                database.execSQL("""
            INSERT INTO HoaDon_new (
                ID, Ten, MaPhong, MaHopDong, NgayTao, Thang, Nam, GiaThue, 
                SODIEN, SONUOC, SoDienTieuThu, SoNuocTieuThu, GiaDichVu, 
                TienMienGiam, TongTien, TrangThai, GhiChu, SoDienCu, SoNuocCu, PhuPhi
            ) SELECT 
                ID, Ten, MaPhong, MaHopDong, NgayTao, Thang, Nam, GiaThue, 
                SODIEN, SONUOC, SoDienTieuThu, SoNuocTieuThu, GiaDichVu, 
                TienMienGiam, TongTien, TrangThai, GhiChu, SoDienCu, SoNuocCu, PhuPhi
            FROM HoaDon
        """)

                // Drop old table and rename new table
                database.execSQL("DROP TABLE HoaDon")
                database.execSQL("ALTER TABLE HoaDon_new RENAME TO HoaDon")
            }
        }

        // Define migration from version 12 to 13 for payment date
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add payment date column (NgayThanhToan) to HoaDon table
                database.execSQL("ALTER TABLE HoaDon ADD COLUMN NgayThanhToan TEXT")

                // Set payment date for already paid bills to current date
                database.execSQL("""
                    UPDATE HoaDon
                    SET NgayThanhToan = date('now')
                    WHERE TrangThai = 1
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
                .createFromAsset(AppConstants.DATABASE_FILE_IMPORT)
                .addMigrations(
                    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
                    MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11,
                    MIGRATION_11_12, MIGRATION_12_13
                )
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

/*
1→2: Thêm thông tin ngân hàng cho NguoiDung
2→3: Thêm trạng thái BiKhoa cho NguoiThue
3→4: Thêm tham chiếu MaKhuTro và MaChuNha cho NguoiThue
4→5: Thêm trạng thái LaChuHopDong cho NguoiThue
5→6: Thêm tùy chọn áp dụng dịch vụ cho tất cả phòng
6→7: Thêm thông tin kết thúc hợp đồng
7→8: Thêm lưu trữ chỉ số điện nước cũ
8→9: Thêm phụ phí cho hóa đơn
9→10: Thêm ghi chú phòng
10→11: Thêm ghi chú sửa chữa
11→12: Thêm liên kết hóa đơn với hợp đồng
12→13: Thêm ngày thanh toán hóa đơn

Khởi tạo cơ sở dữ liệu
Sử dụng Singleton pattern để đảm bảo chỉ có một instance
Tạo database từ file asset có sẵn
Bật foreign key constraints khi mở database
Hỗ trợ fallback migration để tránh lỗi schema
*/
