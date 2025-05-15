package com.app.motel.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.common.utils.showToast
import com.app.motel.core.AppBaseFragment
import com.app.motel.databinding.FragmentProfileDetailBinding
import com.app.motel.feature.tenant.viewmodel.TenantViewModel
import javax.inject.Inject

class ProfileDetailFragment : AppBaseFragment<FragmentProfileDetailBinding>() {

    companion object {
        val ITEM_KEY: String = "TENANT_KEY"
    }

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileDetailBinding {
        return FragmentProfileDetailBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var profileController: UserController

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewmodel : TenantViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(TenantViewModel::class.java)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        init()
        listenStateViewmodel()
    }

    fun init(){
        views.apply {
            btnSave.setOnClickListener {
                val fullName = txtFullName.text.toString()
                val birthDay = txtBirthDay.text.toString()
                val phone = txtPhone.text.toString()
                val email = txtEmail.text.toString()
                val homeTown = txtHomeTown.text.toString()
                val idCard = txtIdCard.text.toString()
                val bankName = txtBank.text.toString() // Add bank name
                val accountNumber = txtNumberBank.text.toString() // Add account number

                viewmodel.updateCurrentUser(
                    currentUser = profileController.state.getCurrentUser,
                    fullName = fullName,
                    birthDay = birthDay,
                    phoneNumber = phone,
                    email = email,
                    homeTown = homeTown,
                    idCard = idCard,
                    password = txtPassword.text.toString(),
                    username = txtUsername.text.toString(),
                    bankName = bankName, // Pass bank name to viewModel
                    accountNumber = accountNumber // Pass account number to viewModel
                )
            }
        }
    }

    private fun listenStateViewmodel() {
        profileController.state.currentUser.observe(viewLifecycleOwner){
            val currentUser = it.data
            views.apply {
                tvTitle.text = if(currentUser?.isAdmin == true) "Thông tin đại diện chủ nhà" else "Thông tin cá nhân"
                txtFullName.setText(currentUser?.name)
                txtBirthDay.setText(currentUser?.birthDate)
                txtPhone.setText(currentUser?.phone)
                txtEmail.setText(currentUser?.email)
                txtIdCard.setText(currentUser?.idCard)
                txtHomeTown.setText(currentUser?.homeTown)
                txtUsername.setText(currentUser?.username)
                txtPassword.setText(currentUser?.password)
                txtBank.setText(currentUser?.bankName)
                txtNumberBank.setText(currentUser?.accountNumber)

                tilEmail.isVisible = it.data?.isAdmin == true
                tilBank.isVisible = it.data?.isAdmin == true
                tilNumberBank.isVisible = it.data?.isAdmin == true
                tilHomeTown.isVisible = it.data?.isAdmin == false
                tilIdCard.isVisible = it.data?.isAdmin == false
            }
        }

        viewmodel.liveData.updateCurrentUser.observe(viewLifecycleOwner){
            if(it.isLoading() || it.isInitialize()) return@observe
            requireActivity().showToast(it.message ?: if(it.isSuccess()) "Thành công" else "Thất bại")
            if(it.isSuccess()) {
                profileController.getCurrentUser()
            }
        }
    }

}