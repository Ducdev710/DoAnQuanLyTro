package com.app.motel.feature.news.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.model.Notification
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room

class NewsViewState: AppViewLiveData {
    val news = MutableLiveData<List<Notification>>()
    val rooms = MutableLiveData<List<Room>>()

    val addNews = MutableLiveData<Resource<Notification>>()
}