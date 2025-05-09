package com.app.motel.feature.auth

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.core.AppBaseFragment
import com.app.motel.databinding.FragmentLoginBinding
import com.app.motel.common.ultis.navigateFragmentWithSlide
import com.app.motel.data.model.Status
import com.app.motel.feature.MainActivity
import com.app.motel.feature.auth.viewmodel.AuthViewModel
import com.app.motel.ui.showLoadingDialog
import javax.inject.Inject

class LoginFragment @Inject constructor() : AppBaseFragment<FragmentLoginBinding>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : AuthViewModel by lazy{
        ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentLoginBinding {
        return FragmentLoginBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        views.btnToRegister.setOnClickListener{
            navigateFragmentWithSlide(R.id.registerFragment)
        }
        views.btnSignin.setOnClickListener{
            viewModel.login(views.txtUserName.text.toString(), views.txtPassword.text.toString())
        }

        handleStateViewModel()
    }


    private var dialogLoading: Dialog? = null
    private fun handleStateViewModel() {
        viewModel.liveData.apply {
            login.observe(viewLifecycleOwner){
                when(it.status) {
                    Status.LOADING -> {
                        dialogLoading = showLoadingDialog(requireContext(), layoutInflater)
                    }
                    Status.SUCCESS -> {
                        dialogLoading?.dismiss()
                        dialogLoading = null

                        requireActivity().apply {
                            finishAffinity()
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                    }
                    Status.ERROR -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                        dialogLoading?.dismiss()
                        dialogLoading = null
                    }
                    Status.INITIALIZE -> {
                        dialogLoading?.dismiss()
                        dialogLoading = null
                    }
                }
            }
        }
    }

}