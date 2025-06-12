package com.app.motel.core

/**
 * Interface đánh dấu cho các sự kiện một lần (one-time events) trong mô hình MVI.
 *
 * Các sự kiện này được sử dụng để thông báo đến View về các hành động cần thực hiện
 * mà không lưu trữ trong trạng thái, ví dụ như hiển thị thông báo, điều hướng màn hình,
 * hoặc hiển thị dialog.
 *
 * Khác với ViewState, ViewEvent thường là các sự kiện tạm thời và chỉ được xử lý một lần.
 * Điều này giúp tránh vấn đề như hiển thị cùng một thông báo nhiều lần khi cấu hình thay đổi.
 *
 * Mỗi màn hình có thể định nghĩa các lớp ViewEvent riêng triển khai interface này,
 * như NotifyViewEvent trong module thông báo.
 */
interface AppViewEvent

/**
 * Đối tượng singleton đại diện cho trường hợp không có sự kiện nào.
 * Được sử dụng khi ViewModel không cần phát sự kiện đến View.
 *
 * Việc sử dụng object thay vì class giúp tối ưu bộ nhớ vì chỉ tạo một instance duy nhất.
 */
object EmptyViewEvents : AppViewEvent