package com.app.motel.common

object AppConstants {
    const val DATABASE_NAME: String = "app_database.db"
    const val DATABASE_FILE_IMPORT: String = "databases/app_database_db.db"

    const val PREFS_NAME: String = "prefs_name"

    /**
     * Key để lưu trữ và truy xuất token xác thực từ SharedPreferences.
     * Được sử dụng trong RemoteDataSource để thêm header Authorization vào API requests.
     * Hiện tại đã cài đặt nhưng chưa sử dụng đầy đủ do chưa tích hợp API thật.
     */
    const val TOKEN_KEY: String = "token_key"

    const val USER_ID_KEY: String = "currentUserId"
    const val BOARDING_HOUSE_ID_KEY: String = "currentBoardingHouseId"

    /**
     * URL cơ sở của API giả lập từ dịch vụ MockAPI.io.
     * Dùng API giả lập trong quá trình phát triển và thử nghiệm ứng dụng.
     * Sử dụng trong quá trình phát triển và chuẩn bị cho việc tích hợp API thật.
     * Khi chuyển sang API production, chỉ cần thay đổi giá trị URL này.
     */
    const val MOCK_BASE_URL = "https://673c599596b8dcd5f3f99525.mockapi.io"
}