package com.app.motel

import android.app.Application
import com.app.motel.di.DaggerAppComponent
import com.app.motel.di.AppComponent
import com.app.motel.feature.profile.UserController
import javax.inject.Inject

class AppApplication : Application() {

    @Inject
    lateinit var profileViewModel: UserController

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        profileViewModel.clearCoroutine()
    }
}