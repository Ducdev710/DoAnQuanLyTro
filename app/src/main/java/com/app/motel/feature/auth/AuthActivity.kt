package com.app.motel.feature.auth

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.core.AppBaseActivity
import com.app.motel.databinding.ActivityAuthBinding
import com.app.motel.feature.auth.viewmodel.AuthViewModel
import javax.inject.Inject

class AuthActivity : AppBaseActivity<ActivityAuthBinding>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : AuthViewModel by lazy{
        ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)
    }

    override fun getBinding(): ActivityAuthBinding {
        return ActivityAuthBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as AppApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(views.root)
    }
}