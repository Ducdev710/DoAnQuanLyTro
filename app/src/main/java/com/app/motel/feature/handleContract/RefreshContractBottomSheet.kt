package com.app.motel.feature.handleContract

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.common.service.DateConverter
import com.app.motel.common.ultis.observe
import com.app.motel.common.ultis.showToast
import com.app.motel.core.AppBaseBottomSheet
import com.app.motel.data.model.Contract
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Status
import com.app.motel.databinding.DialogRefreshContractBinding
import com.app.motel.feature.handleContract.viewmodel.HandleContractViewModel
import javax.inject.Inject

class RefreshContractBottomSheet(
    private val contract: Contract
): AppBaseBottomSheet<DialogRefreshContractBinding>() {
    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogRefreshContractBinding {
        return DialogRefreshContractBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : HandleContractViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(HandleContractViewModel::class.java)
    }

    override val isExpanded: Boolean
        get() = true

    override val isBorderRadiusTop: Boolean
        get() = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        views.tvId.text = contract.name
        views.tvName.text = contract.room?.roomName
        views.txtTimeOldContract.setText(contract.duration?.toString())
        views.txtEndDateOldContract.setText(contract.endDate)

        views.btnCancel.setOnClickListener{
            dismiss()
        }
        views.btnSave.setOnClickListener{
            viewModel.refreshContract(
                contract,
                views.txtTimeNewContract.text.toString().toIntOrNull(),
                views.txtEndDateNewContract.text.toString()
            )
        }

        views.txtTimeNewContract.doOnTextChanged { text, start, before, count ->
            val duration = views.txtTimeNewContract.text.toString().toIntOrNull() ?: 0
            val endDate = DateConverter.localStringToDate(contract.endDate) ?: DateConverter.getCurrentDateTime()
            val newEndDate = DateConverter.addMonthsToDate(endDate, duration)

            views.txtEndDateNewContract.setText(DateConverter.dateToLocalString(newEndDate))
        }
        listenStateViewModel()
    }


    private fun listenStateViewModel() {
        viewModel.liveData.updateContract.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    requireContext().showToast(it.message ?: "Thao tác dịch vụ thành công")
                    dismiss()
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