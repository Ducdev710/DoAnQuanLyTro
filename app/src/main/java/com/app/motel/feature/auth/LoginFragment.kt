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
import com.app.motel.common.utils.navigateFragmentWithSlide
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

    //Phương thức handleStateViewModel() theo dõi LiveData từ ViewModel
    //Xử lý các trạng thái khác nhau của tiến trình đăng nhập:
    //Loading: Hiển thị dialog loading
    //Success: Chuyển đến MainActivity
    //Error: Hiển thị thông báo lỗi
    //Initialize: Reset UI
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

//Fragment này tuân theo mô hình MVVM, sử dụng ViewModel để quản lý logic nghiệp vụ và trạng thái,
//đồng thời sử dụng data binding để kết nối UI với dữ liệu. Nó cũng sử dụng dependency injection
//để dễ dàng kiểm thử và giảm sự phụ thuộc.