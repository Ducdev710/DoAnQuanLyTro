package com.app.motel.core


/**
 * Interface đánh dấu cho các lớp lưu trữ trạng thái UI trong mô hình MVI.
 *
 * Lớp triển khai interface này thường chứa các MutableLiveData để lưu trạng thái
 * của từng thành phần UI, cho phép ViewModel cập nhật và View quan sát.
 *
 * Interface này là một phần của kiến trúc MVI (Model-View-Intent) nơi:
 * - Model: Dữ liệu và logic nghiệp vụ
 * - View: UI hiển thị trạng thái và gửi các action
 * - Intent: Các action từ người dùng được xử lý bởi ViewModel
 *
 * Mỗi màn hình trong ứng dụng nên có một lớp ViewState riêng triển khai
 * interface này để quản lý trạng thái UI của mình.
 */
interface AppViewLiveData
