package com.app.motel.feature.handleContract

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.app.motel.common.AppConstants
import com.app.motel.common.utils.toStringMoney
import com.app.motel.core.AppBaseBottomSheet
import com.app.motel.data.entity.HopDongEntity
import com.app.motel.data.model.Contract
import com.app.motel.databinding.DialogDetailContractBinding

class DetailContractBottomSheet(
    private val contract: Contract,
    private val onUpdateContract: ((Contract) -> Unit)? = null
): AppBaseBottomSheet<DialogDetailContractBinding>() {

    private var isEditMode = false

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogDetailContractBinding {
        return DialogDetailContractBinding.inflate(inflater, container, false)
    }

    override val isExpanded: Boolean
        get() = true

    override val isBorderRadiusTop: Boolean
        get() = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInitialView()
        setupListeners()
    }

    private fun setupInitialView() {
        views.apply {
            tvId.text = contract.name
            tvCraeteDate.text = contract.createdDate
            tvNameRoom.text = contract.room?.roomName
            tvContractOwner.text = contract.tenant?.fullName ?: ""
            tvDuration.text = "Thời gian: ${contract.duration ?: 0} tháng"
            tvDeposit.setText("Tiền cọc: ${contract.deposit.toStringMoney()}")
            tvStartDate.text = contract.startDate
            tvEndDate.text = contract.endDate
            txtNote.setText(contract.note)

            cbEndContract.isChecked = contract.state == Contract.State.ENDED
            cbInactive.isChecked = contract.isActive == HopDongEntity.INACTIVE

            // Set initial editable state
            txtNote.isEnabled = false
            cbEndContract.isEnabled = false
            cbInactive.isEnabled = false

            // Show update button only if callback is provided
            btnUpdate.isVisible = onUpdateContract != null
        }
    }

    private fun setupListeners() {
        views.apply {
            btnEnd.setOnClickListener {
                dismiss()
            }

            // Check if contract is active
            val isActiveContract = contract.isActive == HopDongEntity.ACTIVE

            // Get current user ID from wherever you store it (SharedPreferences, etc.)
            val currentUserId = requireContext().getSharedPreferences(AppConstants.PREFS_NAME, 0)
                .getString(AppConstants.USER_ID_KEY, "") ?: ""

            // Check if current user is the tenant (hide button for tenant)
            val isTenant = currentUserId == contract.customerId

            // Only show update button if:
            // 1. Callback exists
            // 2. Contract is active
            // 3. User is NOT the tenant
            btnUpdate.isVisible = onUpdateContract != null && isActiveContract && !isTenant

            btnUpdate.setOnClickListener {
                if (isEditMode) {
                    // Save changes
                    saveContractChanges()
                } else {
                    // Enter edit mode
                    toggleEditMode(true)
                }
            }
        }
    }

    private fun toggleEditMode(editMode: Boolean) {
        isEditMode = editMode
        views.apply {
            // Only enable note field for editing
            txtNote.isEnabled = editMode
            tvDeposit.isEnabled = editMode

            // Disable other fields
            tvStartDate.isEnabled = false
            tvEndDate.isEnabled = false

            // Disable status checkboxes
            cbEndContract.isEnabled = false
            cbInactive.isEnabled = false

            // Update button text based on mode
            btnUpdate.text = if (editMode) "Lưu" else "Cập nhật"
        }
    }

    private fun saveContractChanges() {
        views.apply {
            // Extract deposit value from text field, removing formatting
            val depositValue = tvDeposit.text.toString()
                .replace("Tiền cọc: ", "")
                .replace("đ", "")
                .trim()

            // Create updated contract with changes
            val updatedContract = contract.copy(
                note = txtNote.text.toString(),
                deposit = depositValue
            )

            // Notify listener about the update
            onUpdateContract?.invoke(updatedContract)

            // Return to view mode
            toggleEditMode(false)
        }
    }
}