package com.app.motel.core

/**
 * Interface đánh dấu cho các hành động (actions) từ View đến ViewModel trong mô hình MVI.
 *
 * Các action đại diện cho ý định (intent) của người dùng hoặc hệ thống,
 * như nhấn nút, nhập liệu, hoặc các sự kiện hệ thống cần được xử lý.
 *
 * Trong mô hình MVI:
 * - View gửi các action đến ViewModel
 * - ViewModel xử lý action và cập nhật trạng thái hoặc phát sự kiện
 * - View quan sát trạng thái và hiển thị UI tương ứng
 *
 * Mỗi màn hình thường định nghĩa một sealed class triển khai interface này
 * để liệt kê tất cả các action có thể xảy ra trên màn hình đó.
 */
interface AppViewActions

/**
 * Đối tượng singleton đại diện cho trường hợp không có action nào.
 * Được sử dụng khi màn hình không cần xử lý bất kỳ action nào từ người dùng.
 *
 * Tương tự EmptyViewEvents, việc sử dụng object thay vì class giúp tối ưu bộ nhớ.
 */
object EmptyAction : AppViewActions