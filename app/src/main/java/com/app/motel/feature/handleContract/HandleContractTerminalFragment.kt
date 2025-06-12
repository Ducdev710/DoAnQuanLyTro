package com.app.motel.feature.handleContract

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.common.service.DateConverter
import com.app.motel.common.utils.popFragmentWithSlide
import com.app.motel.common.utils.showToast
import com.app.motel.common.utils.toMoney
import com.app.motel.common.utils.toStringMoney
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.entity.HopDongEntity
import com.app.motel.data.model.Contract
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Status
import com.app.motel.databinding.FragmentHandleContractTerminalBinding
import com.app.motel.feature.handleContract.viewmodel.HandleContractViewModel
import com.google.gson.Gson
import javax.inject.Inject

class HandleContractTerminalFragment @Inject constructor() : AppBaseFragment<FragmentHandleContractTerminalBinding>() {

    companion object{
        const val CONTRACT_KEY = "contract_key"
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHandleContractTerminalBinding {
        return FragmentHandleContractTerminalBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : HandleContractViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(HandleContractViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        init()
        listenStateViewModel()
    }

    var contract: Contract? = null
    private fun init() {
        //Chuyển đổi chuỗi JSON từ arguments thành đối tượng Contract
        contract = Gson().fromJson(arguments?.getString(CONTRACT_KEY), Contract::class.java)

        views.apply {
            txtDateEnd.setText(DateConverter.getCurrentLocalDateTime())
            tvUserRented.text = contract?.tenant?.fullName
            tvRoomName.text = contract?.room?.roomName
            tvStartDate.text = contract?.startDate
            tvEndDate.text = contract?.endDate
            tvDeposit.text = "${contract?.deposit.toStringMoney()} VND"

            //Hiển thị/ẩn phần bồi thường dựa trên checkbox "Có hư hại"
            lyMoneyCompensation.isVisible = cbHasDamaged.isChecked
            cbHasDamaged.setOnCheckedChangeListener { _, isChecked ->
                lyMoneyCompensation.isVisible = isChecked
            }
            calculateMoneyReturn()
            txtMoneyCompensation.addTextChangedListener {
                calculateMoneyReturn()
            }
            txtDeduction.addTextChangedListener {
                calculateMoneyReturn()
            }

            btnCancel.setOnClickListener{
                popFragmentWithSlide()
            }
            btnSave.setOnClickListener{
                // Lấy thông tin hủy hợp đồng từ các trường nhập liệu
                val terminationReason = txtReson.text.toString()
                val refundAmount = tvMoneyResult.text.toString()
                val deductionReason = txtResonDeduction.text.toString()

                // Tạo hợp đồng cập nhật với thông tin chi tiết chấm dứt
                val updatedContract = contract!!.copy(
                    status = Contract.State.ENDED.ordinal, // Convert enum to int
                    isActive = HopDongEntity.INACTIVE,
                    terminationReason = terminationReason,
                    refundAmount = refundAmount,
                    deductionReason = deductionReason
                )

                // Kết thúc hợp đồng với thông tin được cập nhật
                viewModel.endContract(
                    contract!!,
                    txtDateEnd.text.toString(),
                    cbResultDeposited.isChecked,
                    cbFullyPaid.isChecked,
                    terminationReason,
                    refundAmount,
                    deductionReason
                )
            }
        }
    }

    //Tính toán số tiền hoàn trả = tiền cọc - khấu trừ - bồi thường
    @SuppressLint("SetTextI18n")
    private fun calculateMoneyReturn() {
        val deduction = views.txtDeduction.text.toString().toMoney()
        val deposit = contract?.deposit?.toMoney() ?: 0
        val moneyCompensation = views.txtMoneyCompensation.text.toString().toMoney()

        val moneyReturn = deposit - deduction - moneyCompensation
        views.tvMoneyResult.text = "${moneyReturn.toStringMoney()} VND"
    }

    private fun listenStateViewModel() {
        viewModel.liveData.updateContract.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    requireContext().showToast(it.message ?: "Thao tác dịch vụ thành công")
                    popFragmentWithSlide()
                    viewModel.getContracts()
                    viewModel.liveData.updateContract.postValue(Resource.Initialize())
                }
                Status.ERROR -> {
                    requireContext().showToast(it.message ?: "Có lỗi xảy ra")
                }
                else -> {}
            }
        }
    }
}