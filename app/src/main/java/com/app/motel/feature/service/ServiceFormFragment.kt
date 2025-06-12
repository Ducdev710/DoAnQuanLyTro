package com.app.motel.feature.service

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
        //Key để truyền dữ liệu dịch vụ hiện có (khi sửa)
        const val ITEM_KEY = "service_item"
        //Key để truyền roomId nếu tạo dịch vụ cho một phòng cụ thể
        const val ROOM_ID_KEY = "room_id"
    }

    private var roomId: String? = null

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

        // Lấy roomId từ arguments
        roomId = arguments?.getString(ROOM_ID_KEY)

        init()
        setupValidation()
        listenerViewModelState()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun init() {
        // Lấy dịch vụ từ arguments nếu có (khi sửa)
        val item = Gson().fromJson(arguments?.getString(ITEM_KEY), Service::class.java)
        viewModel.initForm(item)
        views.txtName.setText(item?.name ?: "")

        // Ẩn checkbox nếu đang tạo dịch vụ cho phòng cụ thể
        if (roomId != null) {
            views.cbApplyAllRoom.visibility = View.GONE
        }

        views.lyTypePay.setOnClickListener {
            showDialogSelectTypePay()
        }

        views.btnSave.setOnClickListener{
            viewModel.createService(
                name = views.txtName.text.toString(),
                price = views.txtlPrice.text.toString(),
                typePay = views.tvTypePay.text.toString(),
                isAppliesAllRoom = if (roomId != null) false else views.cbApplyAllRoom.isChecked,
                roomId = roomId
            )
        }

        views.btnDeletel.setOnClickListener{
            if(viewModel.liveData.currentService.value != null){
                viewModel.deleteService(viewModel.liveData.currentService.value)
            }else{
                popFragmentWithSlide()
            }
        }
    }

    private fun setupValidation() {
        // Kiểm tra trạng thái ban đầu - có phải đang tạo dịch vụ mới không
        val isNewService = viewModel.liveData.currentService.value == null

        // Chỉ vô hiệu hóa nút lưu cho dịch vụ chung mới (không phải dịch vụ riêng phòng)
        if (isNewService && roomId == null) {
            views.btnSave.isEnabled = false
        }

        val validateForm = {
            val nameNotEmpty = views.txtName.text.toString().trim().isNotEmpty()
            val priceNotEmpty = views.txtlPrice.text.toString().trim().isNotEmpty()
            val isApplyToAllRooms = views.cbApplyAllRoom.isChecked

            // Đối với dịch vụ chung mới, yêu cầu phải chọn checkbox
            val isValid = nameNotEmpty && priceNotEmpty &&
                    (roomId != null || !isNewService || isApplyToAllRooms)

            views.btnSave.isEnabled = isValid
        }

        // Thêm các text watcher
        views.txtName.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { validateForm() }
        })

        views.txtlPrice.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { validateForm() }
        })

        // Thêm listener cho checkbox (chỉ khi checkbox hiển thị)
        if (roomId == null) {
            views.cbApplyAllRoom.setOnCheckedChangeListener { _, _ -> validateForm() }
        }

        // Initial validation
        validateForm()
    }

    private fun listenerViewModelState() {
        viewModel.liveData.currentService.observe(viewLifecycleOwner){
            views.txtName.setText(it?.name ?: "")
            views.txtlPrice.setText(it?.price.toStringMoney())
            views.tvTypePay.text = it?.typePay ?: DichVuEntity.TypePay.FREE.typeName

            // Chỉ hiển thị và thiết lập checkbox nếu không phải dịch vụ riêng phòng
            if (roomId == null) {
                views.cbApplyAllRoom.isChecked = it?.isAppliesAllRoom ?: false
            }

            //Thiết lập text cho các nút dựa vào chế độ (thêm/sửa)
            views.btnSave.text = if(it == null) "Lưu" else "Cập nhật"
            views.btnDeletel.text = if(it == null) "Đóng" else "Xóa"

            // Validation lại form khi dữ liệu dịch vụ thay đổi
            setupValidation()
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

            // Validation lại form sau khi chọn loại thanh toán
            setupValidation()
        }
    }

    //Xóa form khi fragment bị hủy
    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearForm()
    }
}