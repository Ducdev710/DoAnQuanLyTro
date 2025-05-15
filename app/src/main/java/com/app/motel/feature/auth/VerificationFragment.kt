/*
package com.app.motel.feature.auth

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Status
import com.app.motel.databinding.FragmentVerificationBinding
import com.app.motel.feature.MainActivity
import com.app.motel.feature.auth.viewmodel.AuthViewModel
import com.app.motel.ui.showLoadingDialog
import javax.inject.Inject

class VerificationFragment @Inject constructor() : AppBaseFragment<FragmentVerificationBinding>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: AuthViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentVerificationBinding {
        return FragmentVerificationBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleStateViewModel()

        // Lấy token từ deep link hoặc intent
        val token = arguments?.getString("token")
        if (token != null) {
            verifyToken(token)
        }

        views.btnBackToLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun verifyToken(token: String) {
        viewModel.verifyAccount(token)
    }

    private var dialogLoading: Dialog? = null
    private fun handleStateViewModel() {
        viewModel.liveData.apply {
            verification.observe(viewLifecycleOwner) { result ->
                when(result.status) {
                    Status.LOADING -> {
                        dialogLoading = showLoadingDialog(requireContext(), layoutInflater)
                    }
                    Status.SUCCESS -> {
                        dialogLoading?.dismiss()
                        dialogLoading = null

                        Toast.makeText(requireContext(), "Xác thực tài khoản thành công", Toast.LENGTH_LONG).show()
                        requireActivity().apply {
                            finishAffinity()
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                    }
                    Status.ERROR -> {
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
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

    private fun navigateToLogin() {
        // Navigate back to login screen
        // For example, if you're using Navigation component:
        // findNavController().navigate(R.id.action_verificationFragment_to_loginFragment)
    }
}*/
