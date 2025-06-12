package com.app.motel.core

import androidx.lifecycle.ViewModel
import com.app.motel.common.DataSource
import com.app.motel.common.PublishDataSource

/**
 * ViewModel cơ sở cho ứng dụng, xây dựng theo mô hình MVI (Model-View-Intent).
 * Quản lý trạng thái UI, xử lý các action từ người dùng và phát các sự kiện đến View.
 *
 * @param S Kiểu dữ liệu của LiveData, chứa trạng thái UI
 * @param VA Kiểu dữ liệu của các action từ View
 * @param VE Kiểu dữ liệu của các sự kiện gửi đến View
 * @param liveData Instance của LiveData để lưu trữ và quản lý trạng thái UI
 */
abstract class AppBaseViewModel <S : AppViewLiveData, VA : AppViewActions, VE : AppViewEvent>(
    val liveData: S
) : ViewModel() {

    /**
     * DataSource để phát các sự kiện tạm thời đến View
     * Sử dụng RxJava PublishSubject bên trong để xử lý luồng sự kiện
     */
    protected val _viewEvents = PublishDataSource<VE>()

    /**
     * Giao diện công khai của DataSource sự kiện, chỉ cho phép quan sát
     * View có thể quan sát đối tượng này để nhận các sự kiện từ ViewModel
     */
    val viewEvents: DataSource<VE> = _viewEvents

    /**
     * Xử lý các action được gửi từ View
     * Các lớp con phải triển khai phương thức này để định nghĩa logic xử lý cho mỗi loại action
     *
     * @param action Action cần xử lý
     */
    abstract fun handle(action: VA)
}