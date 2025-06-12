package com.app.motel.feature.news.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.model.Notification
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room

class NewsViewState: AppViewLiveData {
    // Lưu trữ danh sách thông báo/tin tức
    val news = MutableLiveData<List<Notification>>()

    // Lưu trữ danh sách phòng
    // Dùng cho dropdown menu khi tạo thông báo mới để chọn phòng cụ thể hoặc tất cả phòng
    val rooms = MutableLiveData<List<Room>>()

    val addNews = MutableLiveData<Resource<Notification>>()
}