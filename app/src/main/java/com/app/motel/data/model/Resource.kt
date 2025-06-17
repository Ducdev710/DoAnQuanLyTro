package com.app.motel.data.model

import androidx.paging.RemoteMediator
import com.bumptech.glide.load.engine.Initializable

// Lớp generic xử lý trạng thái, kết quả từ các hoạt động bất đồng bộ tương tác với database.
// Tham số kiểu T cho phép bao bọc bất kỳ loại dữ liệu nào
class Resource<out T> (val status: Status, val data: T?, var message: String?) {
    // Đánh dấu xem resource đã được xử lý chưa (cho sự kiện một lần)
    private var hasBeenHandled = false

    // Trả về resource chỉ khi chưa được xử lý, ngăn xử lý nhiều lần
    // Hữu ích cho thông báo một lần như toast/snackbar
    fun getDataIfNotHandled(): Resource<T>? =
        if (hasBeenHandled) null else {
            hasBeenHandled = true
            this
        }

    // Object companion chứa các factory method để tạo các loại Resource
    companion object{
        // Tạo resource ở trạng thái khởi tạo
        fun <T> Initialize(data: T? = null,message: String? = null): Resource<T> = Resource<T>(Status.INITIALIZE, data, message)

        // Tạo resource ở trạng thái đang tải
        fun <T> Loading(data: T? = null,message: String? = null): Resource<T> = Resource<T>(Status.LOADING, data, message)

        // Tạo resource ở trạng thái thành công với dữ liệu và thông báo
        fun <T> Success(data: T?, message: String? = null): Resource<T> = Resource<T>(Status.SUCCESS, data, message)

        // Tạo resource ở trạng thái lỗi với thông báo lỗi
        fun <T> Error(data: T? = null, message: String?): Resource<T> = Resource<T>(Status.ERROR, data, message)
    }

    // Các helper function để kiểm tra trạng thái hiện tại của resource
    fun isInitialize() = status == Status.INITIALIZE
    fun isLoading() = status == Status.LOADING
    fun isSuccess() = status == Status.SUCCESS
    fun isError() = status == Status.ERROR
}