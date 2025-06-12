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

    //Biến isEditMode quản lý trạng thái chỉnh sửa
    private var isEditMode = false

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogDetailContractBinding {
        return DialogDetailContractBinding.inflate(inflater, container, false)
    }

    //Mở rộng bottom sheet
    override val isExpanded: Boolean
        get() = true

    //Được đặt false để không bo góc phần trên
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

            // Thiết lập trạng thái chỉnh sửa ban đầu
            txtNote.isEnabled = false
            cbEndContract.isEnabled = false
            cbInactive.isEnabled = false

            // Hiển thị nút cập nhật chỉ khi có callback
            btnUpdate.isVisible = onUpdateContract != null

            // Hiển thị chi tiết kết thúc hợp đồng nếu đã kết thúc
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

            // Kiểm tra hợp đồng có đang hoạt động
            val isActiveContract = contract.isActive == HopDongEntity.ACTIVE

            // Lấy ID người dùng hiện tại từ SharedPreferences
            val currentUserId = requireContext().getSharedPreferences(AppConstants.PREFS_NAME, 0)
                .getString(AppConstants.USER_ID_KEY, "") ?: ""

            // Kiểm tra người dùng hiện tại có phải là người thuê (Ẩn button với khách thuê)
            val isTenant = currentUserId == contract.customerId

            // Chỉ hiển thị nút cập nhật nếu:
            // 1. Có callback
            // 2. Hợp đồng đang hoạt động
            // 3. Người dùng KHÔNG phải là người thuê
            btnUpdate.isVisible = onUpdateContract != null && isActiveContract && !isTenant

            btnUpdate.setOnClickListener {
                if (isEditMode) {
                    // Lưu thay đổi
                    saveContractChanges()
                } else {
                    // Vào chế độ chỉnh sửa
                    toggleEditMode(true)
                }
            }

            // Thêm chức năng date picker cho ngày bắt đầu
            tvStartDate.setOnClickListener {
                if (isEditMode) {
                    showDatePicker(true)
                }
            }

            // Thêm chức năng date picker cho ngày kết thúc
            tvEndDate.setOnClickListener {
                if (isEditMode) {
                    showDatePicker(false)
                }
            }
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()

        // Phân tích ngày hiện tại nếu có
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
                // Sử dụng ngày hiện tại nếu phân tích thất bại
            }
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Định dạng ngày kiểu dd/MM/yyyy
                val formattedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)

                // Đặt ngày đã chọn vào trường thích hợp
                if (isStartDate) {
                    views.tvStartDate.setText(formattedDate)
                } else {
                    views.tvEndDate.setText(formattedDate)
                }

                // Tính lại thời hạn khi ngày thay đổi
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

            // Cập nhật text thời hạn
            val durationText = "Thời gian: $finalDuration tháng"
            views.tvDuration.text = durationText
        } catch (e: Exception) {
            // Nếu tính toán thất bại, không cập nhật thời hạn
        }
    }

    private fun toggleEditMode(editMode: Boolean) {
        isEditMode = editMode
        views.apply {
            // Chỉ cho phép chỉnh sửa một số trường
            txtNote.isEnabled = editMode
            tvDeposit.isEnabled = editMode

            tvStartDate.isEnabled = editMode
            tvEndDate.isEnabled = editMode

            // Vô hiệu hóa các checkbox trạng thái
            cbEndContract.isEnabled = false
            cbInactive.isEnabled = false

            // Cập nhật text nút dựa vào chế độ
            btnUpdate.text = if (editMode) "Lưu" else "Cập nhật"
        }
    }

    private fun saveContractChanges() {
        views.apply {
            // Trích xuất giá trị tiền cọc từ trường văn bản
            val depositValue = tvDeposit.text.toString()
                .replace("Tiền cọc: ", "")
                .replace("đ", "")
                .trim()

            // Trích xuất giá trị thời hạn từ text
            val durationText = tvDuration.text.toString()
            val durationValue = durationText.replace("Thời gian: ", "")
                .replace(" tháng", "")
                .trim()
                .toIntOrNull() ?: contract.duration

            // Tạo hợp đồng cập nhật với các thay đổi
            val updatedContract = contract.copy(
                note = txtNote.text.toString(),
                deposit = depositValue,
                startDate = tvStartDate.text.toString(),
                endDate = tvEndDate.text.toString(),
                duration = durationValue,
                // Giữ nguyên thông tin kết thúc hợp đồng nếu đã chấm dứt
                terminationReason = contract.terminationReason,
                refundAmount = contract.refundAmount,
                deductionReason = contract.deductionReason
            )

            // Thông báo cho listener về cập nhật
            onUpdateContract?.invoke(updatedContract)

            // Quay lại chế độ xem
            toggleEditMode(false)
        }
    }
}