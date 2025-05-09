package com.app.motel.feature.tenant

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.service.DateConverter
import com.app.motel.common.ultis.observe
import com.app.motel.common.ultis.popFragmentWithSlide
import com.app.motel.common.ultis.setOnEndDrawableClick
import com.app.motel.common.ultis.showToast
import com.app.motel.core.AppBaseDialog
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.entity.NguoiThueEntity
import com.app.motel.data.model.Tenant
import com.app.motel.databinding.DialogDatePickerBinding
import com.app.motel.databinding.FragmentTenantFormBinding
import com.app.motel.feature.tenant.viewmodel.TenantViewModel
import com.google.gson.Gson
import java.util.Calendar
import javax.inject.Inject

class TenantFormFragment : AppBaseFragment<FragmentTenantFormBinding>() {

    companion object {
        val ITEM_KEY: String = "TENANT_KEY"
    }

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTenantFormBinding {
        return FragmentTenantFormBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : TenantViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(TenantViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        listenStateViewModel()

        val tenant: Tenant? = Gson().fromJson(arguments?.getString(ITEM_KEY), Tenant::class.java)
        viewModel.initForm(tenant)

        views.txtBirthDay.setOnEndDrawableClick {
            showDialogBirdDay()
        }
        views.btnAdd.setOnClickListener {
            viewModel.handleTenant(
                tenant = viewModel.liveData.currentTenant.value,
                fullName = views.txtFullName.text.toString(),
                state = views.txtTypeRent.text.toString(),
                phoneNumber = views.txtPhone.text.toString(),
                birthDay = views.txtBirthDay.text.toString(),
                idCard = views.txtCcdc.text.toString(),
                homeTown = views.txtHomeTown.text.toString(),
                username = views.txtUsername.text.toString(),
                password = views.txtPassword.text.toString(),
            )
        }

        views.btnCancel.setOnClickListener {
            popFragmentWithSlide()
        }

        views.btnLock.setOnClickListener {
            val currentTenant = viewModel.liveData.currentTenant.value ?: return@setOnClickListener
            val isLock = currentTenant.isLock

            // Toggle lock state
            viewModel.changeStateTenant(currentTenant, !isLock)

            // Update UI immediately for better user experience
            updateLockButtonUI(!isLock)
        }
    }

    private fun updateLockButtonUI(isLocked: Boolean) {
        views.btnLock.apply {
            val lockIcon = getChildAt(0) as? ImageView
            lockIcon?.setImageResource(
                if (isLocked) R.drawable.baseline_lock_open_24
                else R.drawable.baseline_lock_24
            )

            views.btnLockText.text = if (isLocked) "Mở khóa" else "Khóa"
        }
    }

    private fun listenStateViewModel() {
        viewModel.liveData.currentTenant.observe(viewLifecycleOwner){ tenant ->
            views.lyAdd.isVisible = tenant == null
            views.lyUpdate.isVisible = tenant != null
            views.tvTitle.text = if(tenant == null) "Thêm người thuê mới" else "Sửa người thuê"

            if(tenant != null){
                views.apply {
                    txtFullName.setText(tenant.fullName)
                    txtTypeRent.setText(tenant.status)
                    txtPhone.setText(tenant.phoneNumber)
                    txtBirthDay.setText(tenant.birthDay)
                    txtCcdc.setText(tenant.idCard)
                    txtHomeTown.setText(tenant.homeTown)
                    txtUsername.setText(tenant.username)
                    txtPassword.setText(tenant.password)

                    // Update lock button UI based on current state
                    updateLockButtonUI(tenant.isLock)

                    // Check if tenant is temporarily absent
                    if (tenant.status == NguoiThueEntity.Status.TEMPORARY_ABSENT.value) {
                        // Change button appearance to delete
                        btnUpdate.apply {
                            // Find the child views within the LinearLayout
                            val imageView = getChildAt(0) as ImageView
                            val textView = getChildAt(2) as TextView

                            // Update the text and appearance
                            textView.text = "Xóa"
                            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                            imageView.setImageResource(R.drawable.ic_delete)

                            // Change background
                            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_red_rounded)

                            // Set click listener
                            setOnClickListener {
                                // Show confirmation dialog
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Xác nhận xóa")
                                    .setMessage("Bạn có chắc chắn muốn xóa khách thuê ĐÃ CHUYỂN ĐI này?")
                                    .setPositiveButton("Xóa") { _, _ ->
                                        // Delete tenant
                                        viewModel.deleteTenant(tenant.id)
                                    }
                                    .setNegativeButton("Hủy", null)
                                    .show()
                            }
                        }

                        // For temporary absent tenants, only disable editing but allow lock/unlock
                        disableFormEditing(keepLockEnabled = true)
                    } else {
                        // Regular tenant - keep save button
                        btnUpdate.apply {
                            // Find the child views within the LinearLayout
                            val imageView = getChildAt(0) as ImageView
                            val textView = getChildAt(2) as TextView

                            // Update the text and appearance
                            textView.text = "Lưu"
                            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                            imageView.setImageResource(R.drawable.baseline_edit_24)

                            // Original save logic
                            setOnClickListener {
                                viewModel.handleTenant(
                                    tenant = viewModel.liveData.currentTenant.value,
                                    fullName = views.txtFullName.text.toString(),
                                    state = views.txtTypeRent.text.toString(),
                                    phoneNumber = views.txtPhone.text.toString(),
                                    birthDay = views.txtBirthDay.text.toString(),
                                    idCard = views.txtCcdc.text.toString(),
                                    homeTown = views.txtHomeTown.text.toString(),
                                    username = views.txtUsername.text.toString(),
                                    password = views.txtPassword.text.toString(),
                                )
                            }
                        }

                        // Enable form fields
                        enableFormEditing()
                    }
                }
            } else {
                views.apply {
                    txtTypeRent.setText(NguoiThueEntity.Status.INACTIVE.value)
                }
            }
        }

        viewModel.liveData.updateTenant.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                requireActivity().showToast(it.message ?: "Thành công")
                popFragmentWithSlide()
            }else if (it.isError()){
                requireActivity().showToast(it.message ?: "Có lỗi xảy ra")
            }
        }

        viewModel.liveData.deleteTenant.observe(viewLifecycleOwner) {
            if (it.isSuccess()) {
                requireActivity().showToast("Xóa khách thuê thành công")
                popFragmentWithSlide()
            } else if (it.isError()) {
                requireActivity().showToast(it.message ?: "Xóa khách thuê thất bại")
            }
        }
    }

    private fun disableFormEditing(keepLockEnabled: Boolean = false) {
        views.apply {
            txtFullName.isEnabled = false
            txtTypeRent.isEnabled = false
            txtPhone.isEnabled = false
            txtBirthDay.isEnabled = false
            txtCcdc.isEnabled = false
            txtHomeTown.isEnabled = false
            txtUsername.isEnabled = false
            txtPassword.isEnabled = false
            btnLock.isEnabled = keepLockEnabled
        }
    }

    private fun enableFormEditing() {
        views.apply {
            txtFullName.isEnabled = true
            txtTypeRent.isEnabled = true
            txtPhone.isEnabled = true
            txtBirthDay.isEnabled = true
            txtCcdc.isEnabled = true
            txtHomeTown.isEnabled = true
            txtUsername.isEnabled = true
            txtPassword.isEnabled = true
            btnLock.isEnabled = true
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

        dialog.binding.datePickerDob.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(
            Calendar.DAY_OF_MONTH)
        ) { view, year, monthOfYear, dayOfMonth ->
            calendar.set(year, monthOfYear, dayOfMonth)
            views.txtBirthDay.setText(DateConverter.dateToLocalString(calendar.time))
        }
    }
}