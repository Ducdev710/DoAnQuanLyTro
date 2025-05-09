package com.app.motel.feature.handleBill

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.common.ultis.showToast
import com.app.motel.common.ultis.toStringMoney
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        viewModel.initForm(bill)
        listenStateViewModel()

        views.btnEnd.setOnClickListener{
            if(isPaying){
                viewModel.payingBill(viewModel.liveData.currentBill.value)
            }else{
                dismiss()
            }
        }
    }

    private var isPaying = false
    @SuppressLint("SetTextI18n")
    fun listenStateViewModel(){
        viewModel.liveData.currentBill.observe(viewLifecycleOwner){
            views.apply {
                tvCraeteDate.text = it?.createdDate
                tvNameRoom.text = "Phòng: ${it?.room?.roomName ?: ""}"
                tvTenantName.text = "Tên khách: ${it?.tenant?.fullName ?: ""}"
                tvBillDate.text = "Hóa đơn tháng ${it?.month ?: ""}/${it?.year ?: ""}"
                tvPriceRoom.text = it?.roomPrice?.toInt().toStringMoney() + " VND"
                tvPriceService.text = it?.serviceFee.toStringMoney() + " VND"
                tvElectricityIndex.text = it?.electricityUsed.toStringMoney() + " Số"
                tvWaterIndex.text = it?.waterUsed.toStringMoney() + " Khối"
                tvPriceDiscount.text = it?.discount.toStringMoney() + " VND"
                tvTotal.text = it?.totalAmount.toStringMoney() + " VND"
                cbPayed.isChecked = it?.status == HoaDonEntity.STATUS_PAID

                isPaying = !viewModel.userController.state.isAdmin && it?.status == HoaDonEntity.STATUS_UNPAID
                views.btnEnd.text = if (isPaying) "Thanh toán" else "Đóng"
            }
        }
        viewModel.liveData.updateBill.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    requireActivity().showToast(it.message ?: "Thanh toán thành công")
                    dismiss()
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