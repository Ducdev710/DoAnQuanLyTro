package com.app.motel.feature.news.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.model.Notification
import com.app.motel.data.model.Resource
import com.app.motel.data.repository.NotificationRepository
import com.app.motel.data.repository.RoomRepository
import com.app.motel.feature.profile.UserController
import kotlinx.coroutines.launch
import javax.inject.Inject

class NewsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val roomRepository: RoomRepository,
    val userController: UserController,
): AppBaseViewModel<NewsViewState, NewsViewAction, NewsViewEvent>(NewsViewState()) {
    override fun handle(action: NewsViewAction) {

    }

    fun getNews(){
        viewModelScope.launch {
            try {
                val news = notificationRepository.getNotificationByBoardingHouseId(userController.state.currentBoardingHouseId)
                liveData.news.postValue(news)
            }catch (e: Exception){
                Log.e("NotifyViewModel", "lỗi: getNews: ${e.message}")
            }
        }
    }

    fun getRooms(){
        viewModelScope.launch {
            try {
                val rooms = roomRepository.geRoomBytBoardingHouseId(userController.state.currentBoardingHouseId)
                liveData.rooms.postValue(rooms)
            }catch (e: Exception){
                Log.e("NotifyViewModel", "getRooms: ${e.message}")
            }
        }
    }

    fun addNews(title: String, content: String, roomId: String?){

        when{
            !userController.state.isAdmin -> {
                liveData.addNews.postValue(Resource.Error(message = "Bạn không có quyền thêm tin tức"))
                return
            }
            userController.state.currentBoardingHouseId.isBlank() -> {
                liveData.addNews.postValue(Resource.Error(message = "Bạn chưa chọn khu trọ"))
                return
            }
            title.isBlank() -> {
                liveData.addNews.postValue(Resource.Error(message = "Tiêu đề không được đẻ trống"))
                return
            }
            content.isBlank() -> {
                liveData.addNews.postValue(Resource.Error(message = "Nội dung không được đẻ trống"))
                return
            }
        }

        val news = Notification(
            title = title,
            content = content,
            khuTroId = userController.state.currentBoardingHouseId,
            phongId = roomId,
        )

        viewModelScope.launch {
            val newsAdd = notificationRepository.addNews(news)
            liveData.addNews.postValue(newsAdd)
        }
    }
}