package com.app.motel.feature.handleBill

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.common.utils.showToast
import com.app.motel.common.utils.toStringMoney
import com.app.motel.core.AppBaseBottomSheet
import com.app.motel.data.entity.HoaDonEntity
import com.app.motel.data.model.Bill
import com.app.motel.data.model.Status
import com.app.motel.databinding.DialogDetailBillBinding
import com.app.motel.feature.handleBill.viewmodel.HandleBillViewModel
import javax.inject.Inject

class HandleDetailBillBottomSheet(private val bill: Bill): AppBaseBottomSheet<DialogDetailBillBinding>() {
    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogDetailBillBinding {
        return DialogDetailBillBinding.inflate(inflater, container, false)
    }

    override val isExpanded: Boolean
        get() = true

    override val isBorderRadiusTop: Boolean
        get() = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : HandleBillViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(HandleBillViewModel::class.java)
    }

    private var isPaying = false
    private var isEditMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        viewModel.initForm(bill)
        listenStateViewModel()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        views.btnEnd.setOnClickListener {
            if (isEditMode) {
                // Cancel edit mode
                isEditMode = false
                displayViewMode()
                views.btnEdit.isVisible = viewModel.userController.state.isAdmin &&
                        viewModel.liveData.currentBill.value?.status == HoaDonEntity.STATUS_UNPAID
            } else if (isPaying) {
                viewModel.payingBill(viewModel.liveData.currentBill.value)
            } else {
                dismiss()
            }
        }

        views.btnEdit.setOnClickListener {
            isEditMode = true
            displayEditMode()
        }

        views.btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {
        try {
            val currentBill = viewModel.liveData.currentBill.value?.copy() ?: return

            // Get updated values from edit fields
            val roomPrice = views.edtPriceRoom.text.toString().toIntOrNull() ?: 0
            val serviceFee = views.edtPriceService.text.toString().toIntOrNull() ?: 0
            val electricityIndex = views.edtElectricityNew.text.toString().toIntOrNull() ?: 0
            val waterIndex = views.edtWaterNew.text.toString().toIntOrNull() ?: 0
            val discount = views.edtPriceDiscount.text.toString().toIntOrNull() ?: 0
            val note = views.edtNote.text.toString()

            // Use previous readings directly from the current bill
            val oldElectricityIndex = currentBill.previousElectricityIndex ?: 0
            val oldWaterIndex = currentBill.previousWaterIndex ?: 0

            // Validate new readings
            if (electricityIndex < oldElectricityIndex) {
                requireActivity().showToast("Chỉ số điện mới phải lớn hơn hoặc bằng chỉ số cũ")
                return
            }

            if (waterIndex < oldWaterIndex) {
                requireActivity().showToast("Chỉ số nước mới phải lớn hơn hoặc bằng chỉ số cũ")
                return
            }

            // Calculate consumption based on the difference between new readings and old readings
            val electricityUsed = electricityIndex - oldElectricityIndex
            val waterUsed = waterIndex - oldWaterIndex

            // Update bill with new values
            val updatedBill = currentBill.copy(
                roomPrice = roomPrice.toDouble(),
                serviceFee = serviceFee.toString(),
                electricityIndex = electricityIndex,
                waterIndex = waterIndex,
                electricityUsed = electricityUsed,
                waterUsed = waterUsed,
                discount = discount.toString(),
                note = note
            )

            // Let the ViewModel handle the update and total calculation
            viewModel.updateBill(updatedBill)
        } catch (e: Exception) {
            requireActivity().showToast("Lỗi: ${e.message}")
        }
    }

    private fun displayEditMode() {
        val bill = viewModel.liveData.currentBill.value

        // Show edit fields, hide text views
        views.apply {
            // Room price
            tvPriceRoom.isVisible = false
            edtPriceRoom.isVisible = true
            edtPriceRoom.setText(bill?.roomPrice?.toInt()?.toString() ?: "0")

            // Service fee
            tvPriceService.isVisible = false
            edtPriceService.isVisible = true
            edtPriceService.setText(bill?.serviceFee?.replace(" VND", "")?.replace(",", "")?.replace(" ", "") ?: "0")

            // Electricity readings
            tvElectricityOld.text = (bill?.previousElectricityIndex ?: 0).toString()
            tvElectricityNew.isVisible = false
            edtElectricityNew.isVisible = true
            edtElectricityNew.setText(bill?.electricityIndex?.toString() ?: "0")

            // Water readings
            tvWaterOld.text = (bill?.previousWaterIndex ?: 0).toString()
            tvWaterNew.isVisible = false
            edtWaterNew.isVisible = true
            edtWaterNew.setText(bill?.waterIndex?.toString() ?: "0")

            // Discount
            tvPriceDiscount.isVisible = false
            edtPriceDiscount.isVisible = true
            edtPriceDiscount.setText(bill?.discount?.replace(" VND", "")?.replace(",", "")?.replace(" ", "") ?: "0")

            // Note
            tvNote.isVisible = false
            edtNote.isVisible = true
            edtNote.setText(bill?.note ?: "")

            // Update buttons
            btnEnd.text = "Hủy"
            btnEdit.isVisible = false
            btnSave.isVisible = true
        }
    }

    private fun displayViewMode() {
        views.apply {
            // Show text views, hide edit fields
            tvPriceRoom.isVisible = true
            edtPriceRoom.isVisible = false

            tvPriceService.isVisible = true
            edtPriceService.isVisible = false

            // Electricity readings
            tvElectricityNew.isVisible = true
            edtElectricityNew.isVisible = false

            // Water readings
            tvWaterNew.isVisible = true
            edtWaterNew.isVisible = false

            tvPriceDiscount.isVisible = true
            edtPriceDiscount.isVisible = false

            tvNote.isVisible = true
            edtNote.isVisible = false

            // Update buttons
            btnEnd.text = if (isPaying) "Thanh toán" else "Đóng"
            btnEdit.isVisible = viewModel.userController.state.isAdmin &&
                    viewModel.liveData.currentBill.value?.status == HoaDonEntity.STATUS_UNPAID
            btnSave.isVisible = false
        }
    }

    @SuppressLint("SetTextI18n")
    fun listenStateViewModel() {
        viewModel.liveData.currentBill.observe(viewLifecycleOwner) { bill ->
            views.apply {
                tvCraeteDate.text = bill?.createdDate
                tvNameRoom.text = "Phòng: ${bill?.room?.roomName ?: ""}"
                tvTenantName.text = "Tên khách: ${bill?.tenant?.fullName ?: ""}"
                tvBillDate.text = "Hóa đơn tháng ${bill?.month ?: ""}/${bill?.year ?: ""}"

                // Format monetary values consistently
                tvPriceRoom.text = "${bill?.roomPrice?.toInt()?.toStringMoney()} VND"
                tvPriceService.text = "${bill?.serviceFee?.toStringMoney()} VND"

                // Display meter readings
                tvElectricityOld.text = (bill?.previousElectricityIndex ?: 0).toString()
                tvElectricityNew.text = (bill?.electricityIndex ?: 0).toString()
                tvElectricityIndex.text = "${bill?.electricityUsed ?: 0} số"

                tvWaterOld.text = (bill?.previousWaterIndex ?: 0).toString()
                tvWaterNew.text = (bill?.waterIndex ?: 0).toString()
                tvWaterIndex.text = "${bill?.waterUsed ?: 0} khối"

                tvPriceDiscount.text = "${bill?.discount?.toStringMoney()} VND"

                // Parse total amount from bill or calculate if missing
                val totalAmount = if (!bill?.totalAmount.isNullOrBlank()) {
                    try {
                        bill?.totalAmount
                            ?.replace(" VND", "")
                            ?.replace(",", "")
                            ?.replace(" ", "")
                            ?.toIntOrNull() ?: 0
                    } catch (e: Exception) {
                        0
                    }
                } else {
                    0
                }

                tvTotal.text = "${totalAmount.toStringMoney()} VND"

                cbPayed.isChecked = bill?.status == HoaDonEntity.STATUS_PAID

                // Display note if available
                layoutNote.isVisible = !bill?.note.isNullOrBlank() || isEditMode
                tvNote.text = bill?.note ?: ""

                // Show edit button only for admin and unpaid bills
                isPaying = !viewModel.userController.state.isAdmin && bill?.status == HoaDonEntity.STATUS_UNPAID
                btnEdit.isVisible = viewModel.userController.state.isAdmin &&
                        bill?.status == HoaDonEntity.STATUS_UNPAID && !isEditMode

                // Update button text
                if (!isEditMode) {
                    btnEnd.text = if (isPaying) "Thanh toán" else "Đóng"
                }
            }
        }

        viewModel.liveData.updateBill.observe(viewLifecycleOwner) {
            when(it.status) {
                Status.SUCCESS -> {
                    requireActivity().showToast(it.message ?: "Cập nhật thành công")
                    if (isEditMode) {
                        isEditMode = false
                        displayViewMode()
                    } else {
                        dismiss()
                    }
                }
                Status.ERROR -> {
                    requireActivity().showToast(it.message ?: "Có lỗi xảy ra")
                }
                else -> {}
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.clearForm()
    }
}