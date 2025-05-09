package com.app.motel.feature.home.viewmodel

import com.app.motel.core.AppViewEvent

sealed class HomeViewEvent : AppViewEvent {
    object ReturnTestViewEvent : HomeViewEvent()
}