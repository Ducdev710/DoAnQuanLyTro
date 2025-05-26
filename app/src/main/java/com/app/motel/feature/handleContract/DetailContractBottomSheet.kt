package com.app.motel.feature.handleContract

import android.app.DatePickerDialog
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
            tvStartDate.setText(contract.startDate ?: "")
            tvEndDate.setText(contract.endDate ?: "")
            txtNote.setText(contract.note)

            cbEndContract.isChecked = contract.state == Contract.State.ENDED
            cbInactive.isChecked = contract.isActive == HopDongEntity.INACTIVE

            // Set initial editable state
            txtNote.isEnabled = false
            cbEndContract.isEnabled = false
            cbInactive.isEnabled = false

            // Show update button only if callback is provided
            btnUpdate.isVisible = onUpdateContract != null

            // Show termination details if contract is ended
            if (contract.state == Contract.State.ENDED) {
                layoutTerminationDetails.isVisible = true
                tvTerminationReason.text = contract.terminationReason ?: ""
                tvRefundAmount.text = contract.refundAmount ?: ""
                tvDeductionReason.text = contract.deductionReason ?: ""
            } else {
                layoutTerminationDetails.isVisible = false
            }
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

            // Add date picker functionality for start date
            tvStartDate.setOnClickListener {
                if (isEditMode) {
                    showDatePicker(true)
                }
            }

            // Add date picker functionality for end date
            tvEndDate.setOnClickListener {
                if (isEditMode) {
                    showDatePicker(false)
                }
            }
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()

        // Parse current date if available
        val currentDate = if (isStartDate) views.tvStartDate.text.toString() else views.tvEndDate.text.toString()
        if (currentDate.isNotEmpty()) {
            try {
                val parts = currentDate.split("/")
                if (parts.size == 3) {
                    val day = parts[0].toInt()
                    val month = parts[1].toInt() - 1 // Calendar months are 0-based
                    val year = parts[2].toInt()
                    calendar.set(year, month, day)
                }
            } catch (e: Exception) {
                // Use current date if parsing fails
            }
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Format the date as dd/MM/yyyy
                val formattedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)

                // Set the selected date to the appropriate field
                if (isStartDate) {
                    views.tvStartDate.setText(formattedDate)
                } else {
                    views.tvEndDate.setText(formattedDate)
                }

                // Recalculate duration whenever dates change
                recalculateDuration()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun recalculateDuration() {
        try {
            val startDateStr = views.tvStartDate.text.toString()
            val endDateStr = views.tvEndDate.text.toString()

            if (startDateStr.isEmpty() || endDateStr.isEmpty()) return

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val startDate = dateFormat.parse(startDateStr) ?: return
            val endDate = dateFormat.parse(endDateStr) ?: return

            val startCalendar = Calendar.getInstance()
            startCalendar.time = startDate

            val endCalendar = Calendar.getInstance()
            endCalendar.time = endDate

            // Tính toán số tháng giữa hai ngày
            val yearDiff = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)
            val monthDiff = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH)

            // Công thức tính thời hạn theo tháng
            var months = yearDiff * 12 + monthDiff

            // Nếu là cùng ngày trong tháng hoặc ngày cuối > ngày đầu thì tính tròn 1 tháng
            // Nếu ngày cuối < ngày đầu thì không tính tròn tháng
            if (endCalendar.get(Calendar.DAY_OF_MONTH) > startCalendar.get(Calendar.DAY_OF_MONTH)) {
                months += 1
            }

            // Trường hợp đặc biệt: nếu chính xác 1 tháng (ví dụ: 25/7 - 25/8)
            if (months == 1 && endCalendar.get(Calendar.DAY_OF_MONTH) == startCalendar.get(Calendar.DAY_OF_MONTH)) {
                months = 1
            }

            // Đảm bảo thời hạn không âm
            val finalDuration = months.coerceAtLeast(0)

            // Update duration text
            val durationText = "Thời gian: $finalDuration tháng"
            views.tvDuration.text = durationText
        } catch (e: Exception) {
            // If calculation fails, don't update the duration
        }
    }

    private fun toggleEditMode(editMode: Boolean) {
        isEditMode = editMode
        views.apply {
            // Only enable note field for editing
            txtNote.isEnabled = editMode
            tvDeposit.isEnabled = editMode

            tvStartDate.isEnabled = editMode
            tvEndDate.isEnabled = editMode

            // Disable status checkboxes
            cbEndContract.isEnabled = false
            cbInactive.isEnabled = false

            // Update button text based on mode
            btnUpdate.text = if (editMode) "Lưu" else "Cập nhật"
        }
    }

    private fun saveContractChanges() {
        views.apply {
            // Extract deposit value from text field
            val depositValue = tvDeposit.text.toString()
                .replace("Tiền cọc: ", "")
                .replace("đ", "")
                .trim()

            // Extract duration value from the text
            val durationText = tvDuration.text.toString()
            val durationValue = durationText.replace("Thời gian: ", "")
                .replace(" tháng", "")
                .trim()
                .toIntOrNull() ?: contract.duration

            // Create updated contract with changes
            val updatedContract = contract.copy(
                note = txtNote.text.toString(),
                deposit = depositValue,
                startDate = tvStartDate.text.toString(),
                endDate = tvEndDate.text.toString(),
                duration = durationValue,
                // Preserve termination details if contract is already terminated
                terminationReason = contract.terminationReason,
                refundAmount = contract.refundAmount,
                deductionReason = contract.deductionReason
            )

            // Notify listener about the update
            onUpdateContract?.invoke(updatedContract)

            // Return to view mode
            toggleEditMode(false)
        }
    }
}