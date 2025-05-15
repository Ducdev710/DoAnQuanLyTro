package com.app.motel.feature.service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.motel.AppApplication
import com.app.motel.common.utils.popFragmentWithSlide
import com.app.motel.common.utils.showToast
import com.app.motel.common.utils.toStringMoney
import com.app.motel.core.AppBaseDialog
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.entity.DichVuEntity
import com.app.motel.data.model.Service
import com.app.motel.data.model.Status
import com.app.motel.databinding.DialogServiceTypePayBinding
import com.app.motel.databinding.FragmentServiceFormBinding
import com.app.motel.feature.service.viewmodel.ServiceViewModel
import com.google.gson.Gson
import javax.inject.Inject

class ServiceFormFragment @Inject constructor() : AppBaseFragment<FragmentServiceFormBinding>() {
    companion object{
        const val ITEM_KEY = "service_item"
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentServiceFormBinding {
        return FragmentServiceFormBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : ServiceViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(ServiceViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        init()
        listenerViewModelState()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun init() {
        val item = Gson().fromJson(arguments?.getString(ITEM_KEY), Service::class.java)
        viewModel.initForm(item)
        views.txtName.setText(item?.name ?: "")

        views.lyTypePay.setOnClickListener {
            showDialogSelectTypePay()
        }

        views.btnSave.setOnClickListener{
            viewModel.createService(
                views.txtName.text.toString(),
                views.txtlPrice.text.toString(),
                views.tvTypePay.text.toString(),
                views.cbApplyAllRoom.isChecked
            )
        }
        views.btnDeletel.setOnClickListener{
            viewModel.deleteService(viewModel.liveData.currentService.value)
        }
        views.btnDeletel.setOnClickListener{
            if(viewModel.liveData.currentService.value != null){
                viewModel.deleteService(viewModel.liveData.currentService.value)
            }else{
                popFragmentWithSlide()
            }
        }
    }

    private fun listenerViewModelState() {
        viewModel.liveData.currentService.observe(viewLifecycleOwner){
            views.txtName.setText(it?.name ?: "")
            views.txtlPrice.setText(it?.price.toStringMoney())
            views.tvTypePay.text = it?.typePay ?: DichVuEntity.TypePay.FREE.typeName
            views.cbApplyAllRoom.isChecked = it?.isAppliesAllRoom ?: false
            views.btnSave.text = if(it == null) "Lưu" else "Cập nhật"
            views.btnDeletel.text = if(it == null) "Đóng" else "Xóa"
        }

        viewModel.liveData.createService.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    requireContext().showToast(it.message ?: "Thao tác dịch vụ thành công")
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                Status.ERROR -> {
                    requireContext().showToast(it.message ?: "Có lỗi xảy ra")
                }
                else -> {}
            }
        }
    }

    private fun showDialogSelectTypePay(){
        val dialog = AppBaseDialog.Builder(requireContext(), DialogServiceTypePayBinding.inflate(layoutInflater))
            .isBorderRadius(false)
            .build()
        dialog.show()

        val adapter = ServiceItemTypePayAdapter(DichVuEntity.TypePay.getType(views.tvTypePay.text.toString()))
        dialog.binding.rcv.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        dialog.binding.rcv.adapter = adapter
        dialog.binding.btnConfirm.setOnClickListener{
            views.tvTypePay.text = adapter.getCurrentItem.typeName
            dialog.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearForm()
    }
}