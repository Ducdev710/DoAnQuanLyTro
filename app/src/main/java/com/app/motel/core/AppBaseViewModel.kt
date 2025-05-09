package com.app.motel.core

import androidx.lifecycle.ViewModel
import com.app.motel.common.DataSource
import com.app.motel.common.PublishDataSource

abstract class AppBaseViewModel <S : AppViewLiveData, VA : AppViewActions, VE : AppViewEvent>(
    val liveData: S
) : ViewModel() {

    // Used to post transient events to the View
    protected val _viewEvents = PublishDataSource<VE>()
    val viewEvents: DataSource<VE> = _viewEvents

    abstract fun handle(action: VA)
}