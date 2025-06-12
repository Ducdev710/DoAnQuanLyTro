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
                // Hủy chế độ chỉnh sửa
                isEditMode = false
                displayViewMode()
                views.btnEdit.isVisible = viewModel.userController.state.isAdmin &&
                        viewModel.liveData.currentBill.value?.status == HoaDonEntity.STATUS_UNPAID
            } else if (isPaying) {
                // Thực hiện thanh toán
                viewModel.payingBill(viewModel.liveData.currentBill.value)
            } else {
                // Đóng bottom sheet
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

            // Lấy dữ liệu từ các trường nhập liệu
            val roomPrice = views.edtPriceRoom.text.toString().toIntOrNull() ?: 0
            val serviceFee = views.edtPriceService.text.toString().toIntOrNull() ?: 0
            val additionalFee = views.edtAdditionalFee.text.toString().toIntOrNull() ?: 0

            // Lấy chỉ số điện nước cũ và mới
            val previousElectricityIndex = views.edtElectricityOld.text.toString().toIntOrNull() ?: 0
            val previousWaterIndex = views.edtWaterOld.text.toString().toIntOrNull() ?: 0

            val electricityIndex = views.edtElectricityNew.text.toString().toIntOrNull() ?: 0
            val waterIndex = views.edtWaterNew.text.toString().toIntOrNull() ?: 0

            val discount = views.edtPriceDiscount.text.toString().toIntOrNull() ?: 0
            val note = views.edtNote.text.toString()

            // Kiểm tra hợp lệ
            if (electricityIndex < previousElectricityIndex) {
                requireActivity().showToast("Chỉ số điện mới phải lớn hơn hoặc bằng chỉ số cũ")
                return
            }

            if (waterIndex < previousWaterIndex) {
                requireActivity().showToast("Chỉ số nước mới phải lớn hơn hoặc bằng chỉ số cũ")
                return
            }

            // Tính lượng tiêu thụ
            val electricityUsed = electricityIndex - previousElectricityIndex
            val waterUsed = waterIndex - previousWaterIndex

            // Cập nhật hóa đơn
            val updatedBill = currentBill.copy(
                roomPrice = roomPrice.toDouble(),
                serviceFee = serviceFee.toString(),
                additionalFee = additionalFee.toString(),
                previousElectricityIndex = previousElectricityIndex,
                previousWaterIndex = previousWaterIndex,
                electricityIndex = electricityIndex,
                waterIndex = waterIndex,
                electricityUsed = electricityUsed,
                waterUsed = waterUsed,
                discount = discount.toString(),
                note = note
            )

            // Gửi cập nhật đến ViewModel
            viewModel.updateBill(updatedBill)
        } catch (e: Exception) {
            requireActivity().showToast("Lỗi: ${e.message}")
        }
    }

    private fun displayEditMode() {
        val bill = viewModel.liveData.currentBill.value

        // Hiển thị các trường nhập liệu, ẩn các TextView
        views.apply {
            // Tiền phòng
            tvPriceRoom.isVisible = false
            edtPriceRoom.isVisible = true
            edtPriceRoom.setText(bill?.roomPrice?.toInt()?.toString() ?: "0")

            // Phí dịch vụ
            tvPriceService.isVisible = false
            edtPriceService.isVisible = true
            edtPriceService.setText(bill?.serviceFee?.replace(" VND", "")?.replace(",", "")?.replace(" ", "") ?: "0")

            // Phi thêm
            tvAdditionalFee.isVisible = false
            edtAdditionalFee.isVisible = true
            edtAdditionalFee.setText(bill?.additionalFee?.replace(" VND", "")?.replace(",", "")?.replace(" ", "") ?: "0")

            // Chỉ số điện - OLD(có thể chỉnh sửa)
            tvElectricityOld.isVisible = false
            edtElectricityOld.isVisible = true
            edtElectricityOld.setText((bill?.previousElectricityIndex ?: 0).toString())

            // Chỉ số điện - NEW (hiển thị và có thể chỉnh sửa)
            tvElectricityNew.isVisible = false
            edtElectricityNew.isVisible = true
            edtElectricityNew.setText(bill?.electricityIndex?.toString() ?: "0")

            // Chỉ số nuoc - OLD (có thể chỉnh sửa)
            tvWaterOld.isVisible = false
            edtWaterOld.isVisible = true
            edtWaterOld.setText((bill?.previousWaterIndex ?: 0).toString())

            // Chỉ số nước - NEW (hiển thị và có thể chỉnh sửa)
            tvWaterNew.isVisible = false
            edtWaterNew.isVisible = true
            edtWaterNew.setText(bill?.waterIndex?.toString() ?: "0")

            // Tiền miên giảm
            tvPriceDiscount.isVisible = false
            edtPriceDiscount.isVisible = true
            edtPriceDiscount.setText(bill?.discount?.replace(" VND", "")?.replace(",", "")?.replace(" ", "") ?: "0")

            // Ghi chú
            tvNote.isVisible = false
            edtNote.isVisible = true
            edtNote.setText(bill?.note ?: "")

            // Cập nhật trạng thái các nút
            btnEnd.text = "Hủy"
            btnEdit.isVisible = false
            btnSave.isVisible = true
        }
    }

    private fun displayViewMode() {
        views.apply {
            // Hiển thị các TextView, ẩn các trường nhập liệu
            tvPriceRoom.isVisible = true
            edtPriceRoom.isVisible = false

            tvPriceService.isVisible = true
            edtPriceService.isVisible = false

            tvAdditionalFee.isVisible = true
            edtAdditionalFee.isVisible = false

            // Chỉ số điện - hiển thị OLD và NEW
            tvElectricityOld.isVisible = true
            edtElectricityOld.isVisible = false

            tvElectricityNew.isVisible = true
            edtElectricityNew.isVisible = false

            // Chis số nước - hiển thị OLD và NEW
            tvWaterOld.isVisible = true
            edtWaterOld.isVisible = false

            tvWaterNew.isVisible = true
            edtWaterNew.isVisible = false

            tvPriceDiscount.isVisible = true
            edtPriceDiscount.isVisible = false

            tvNote.isVisible = true
            edtNote.isVisible = false

            // Cập nhật trạng thái các nút
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
                tvCreateDate.text = bill?.createdDate
                tvNameRoom.text = "Phòng: ${bill?.room?.roomName ?: ""}"

                // Hiển thị tên người thuê chỉ khi người xem là admin
                tvTenantName.isVisible = viewModel.userController.state.isAdmin
                if (viewModel.userController.state.isAdmin) {
                    tvTenantName.text = "Tên khách: ${bill?.tenant?.fullName ?: ""}"
                }

                tvBillDate.text = "Hóa đơn tháng ${bill?.month ?: ""}/${bill?.year ?: ""}"

                // Hiển thị ngày thanh toán nếu có(hóa đơn đã thanh toán)
                tvPaymentDate.apply {
                    isVisible = !bill?.paymentDate.isNullOrBlank()
                    text = bill?.paymentDate ?: ""
                }
                layoutPaymentDate.isVisible = !bill?.paymentDate.isNullOrBlank()

                // Định dạng số tiền
                tvPriceRoom.text = "${bill?.roomPrice?.toInt()?.toStringMoney()} VND"
                tvPriceService.text = "${bill?.serviceFee?.toStringMoney()} VND"
                tvAdditionalFee.text = "${bill?.additionalFee?.toStringMoney()} VND"

                // Hiển thị chỉ số điện nước
                tvElectricityOld.text = (bill?.previousElectricityIndex ?: 0).toString()
                tvElectricityNew.text = (bill?.electricityIndex ?: 0).toString()
                tvElectricityIndex.text = "${bill?.electricityUsed ?: 0} số"

                tvWaterOld.text = (bill?.previousWaterIndex ?: 0).toString()
                tvWaterNew.text = (bill?.waterIndex ?: 0).toString()
                tvWaterIndex.text = "${bill?.waterUsed ?: 0} khối"

                tvPriceDiscount.text = "${bill?.discount?.toStringMoney()} VND"

                // Tính toán và hiển thị tổng số tiền
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

                // Hiên thị ghi chú nếu có
                layoutNote.isVisible = !bill?.note.isNullOrBlank() || isEditMode
                tvNote.text = bill?.note ?: ""

                // Cập nhật trạng thái nút chỉnh sửa (chỉ cho admin và hóa đơn chưa thanh toán)
                isPaying = !viewModel.userController.state.isAdmin && bill?.status == HoaDonEntity.STATUS_UNPAID
                btnEdit.isVisible = viewModel.userController.state.isAdmin &&
                        bill?.status == HoaDonEntity.STATUS_UNPAID && !isEditMode

                // Cập nhật text nút
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