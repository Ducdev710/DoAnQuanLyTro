/*
package com.app.motel.feature.auth

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.utils.navigateFragmentWithSlide
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Status
import com.app.motel.databinding.FragmentOtpVerificationBinding
import com.app.motel.feature.auth.viewmodel.AuthViewModel
import com.app.motel.ui.showLoadingDialog
import javax.inject.Inject

class OtpVerificationFragment @Inject constructor() : AppBaseFragment<FragmentOtpVerificationBinding>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : AuthViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)
    }

    private val args: OtpVerificationFragmentArgs by navArgs()

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentOtpVerificationBinding {
        return FragmentOtpVerificationBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        handleStateViewModel()
    }

    private fun setupListeners() {
        views.btnVerify.setOnClickListener {
            val otp = views.etOtpCode.text.toString().trim()
            if (otp.isEmpty() || otp.length != 6) {
                Toast.makeText(requireContext(), "Vui lòng nhập mã OTP 6 số", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.verifyOtpAndRegister(args.phoneNumber, otp)
        }

        views.tvResendOtp.setOnClickListener {
            Toast.makeText(requireContext(), "Đang gửi lại mã OTP...", Toast.LENGTH_SHORT).show()
            // Implement resend functionality
        }
    }

    private var dialogLoading: Dialog? = null
    private fun handleStateViewModel() {
        viewModel.liveData.apply {
            register.observe(viewLifecycleOwner) {
                when(it.status) {
                    Status.LOADING -> {
                        dialogLoading = showLoadingDialog(requireContext(), layoutInflater)
                        views.btnVerify.isEnabled = false
                    }
                    Status.SUCCESS -> {
                        dialogLoading?.dismiss()
                        dialogLoading = null
                        views.btnVerify.isEnabled = true

                        // Navigate to login screen
                        navigateFragmentWithSlide(R.id.loginFragment)
                        Toast.makeText(requireContext(), "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                    }
                    Status.ERROR -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                        dialogLoading?.dismiss()
                        dialogLoading = null
                        views.btnVerify.isEnabled = true
                    }
                    Status.INITIALIZE -> {
                        dialogLoading?.dismiss()
                        dialogLoading = null
                        views.btnVerify.isEnabled = true
                    }
                }
            }
        }
    }
}*/
