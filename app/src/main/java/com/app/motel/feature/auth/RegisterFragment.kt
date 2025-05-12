package com.app.motel.feature.auth

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.common.service.DateConverter
import com.app.motel.common.ultis.popFragmentWithSlide
import com.app.motel.common.ultis.setOnEndDrawableClick
import com.app.motel.core.AppBaseDialog
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Status
import com.app.motel.databinding.DialogDatePickerBinding
import com.app.motel.databinding.FragmentRegisterBinding
import com.app.motel.feature.MainActivity
import com.app.motel.feature.auth.viewmodel.AuthViewModel
import com.app.motel.ui.showLoadingDialog
import java.util.Calendar
import javax.inject.Inject

class RegisterFragment @Inject constructor() : AppBaseFragment<FragmentRegisterBinding>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : AuthViewModel by lazy{
        ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRegisterBinding {
        return FragmentRegisterBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        views.txtBirthDay.setOnEndDrawableClick {
            showDialogBirdDay()
        }
        views.btnToLogin.setOnClickListener{
            popFragmentWithSlide()
        }
        views.btnSignup.setOnClickListener{
            viewModel.register(
                views.txtName.text.toString(),
                views.txtUserName.text.toString(),
                views.txtPassword.text.toString(),
                views.txtEmail.text.toString(),
                views.txtBirthDay.text.toString(),
                views.txtPhoneNumber.text.toString(),
            )
        }

        handleStateViewModel()
    }

    private var dialogLoading: Dialog? = null
    private fun handleStateViewModel() {
        viewModel.liveData.apply {
            register.observe(viewLifecycleOwner){
                when(it.status) {
                    Status.LOADING -> {
                        dialogLoading = showLoadingDialog(requireContext(), layoutInflater)
                        Log.e("AuthViewModel", "loading register")
                    }
                    Status.SUCCESS -> {
                        Toast.makeText(requireContext(), "Đăng ký thành công", Toast.LENGTH_LONG).show()
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
    private fun showDialogBirdDay(){
        val dialog = AppBaseDialog.Builder(requireContext(), DialogDatePickerBinding.inflate(layoutInflater))
            .build()
        dialog.show()

        dialog.setOnDismissListener {

        }

        val calendar: Calendar = Calendar.getInstance().apply {
            time = DateConverter.localStringToDate(views.txtBirthDay.text.toString())
                ?: DateConverter.localStringToDate("1/1/2000")
                        ?: DateConverter.getCurrentDateTime()
        }

        dialog.binding.datePickerDob.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ) { view, year, monthOfYear, dayOfMonth ->
            calendar.set(year, monthOfYear, dayOfMonth)
            views.txtBirthDay.setText(DateConverter.dateToLocalString(calendar.time))
        }
    }

}