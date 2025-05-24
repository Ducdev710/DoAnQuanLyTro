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
import com.app.motel.data.model.BoardingHouse
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
                val bankName = txtBank.text.toString()
                val accountNumber = txtNumberBank.text.toString()

                // Save user info
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
                    bankName = bankName,
                    accountNumber = accountNumber
                )

                // If user is admin and electricity/water price fields are visible, update boarding house
                if (profileController.state.getCurrentUser?.isAdmin == true &&
                    tilElectricityPrice.isVisible &&
                    tilWaterPrice.isVisible) {

                    val currentBoardingHouse = profileController.state.getCurrentBoardingHouse
                    currentBoardingHouse?.let {
                        try {
                            val electricityPrice = txtElectricityPrice.text.toString().toIntOrNull() ?: 3500
                            val waterPrice = txtWaterPrice.text.toString().toIntOrNull() ?: 20000

                            val updatedBoardingHouse = it.copy(
                                giaDien = electricityPrice,
                                giaNuoc = waterPrice
                            )

                            profileController.setCurrentBoardingHouse(updatedBoardingHouse)
                        } catch (e: Exception) {
                            requireActivity().showToast("Lỗi cập nhật giá điện nước: ${e.message}")
                        }
                    }
                }
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

                // Show electricity and water price fields only for admins
                tilElectricityPrice.isVisible = it.data?.isAdmin == true
                tilWaterPrice.isVisible = it.data?.isAdmin == true
            }
        }

        // Observe the current boarding house to display electricity and water prices
        profileController.state.currentBoardingHouse.observe(viewLifecycleOwner) { resource ->
            resource.data?.let { boardingHouse ->
                views.apply {
                    txtElectricityPrice.setText(boardingHouse.giaDien.toString())
                    txtWaterPrice.setText(boardingHouse.giaNuoc.toString())
                }
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