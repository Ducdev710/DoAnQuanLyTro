package com.app.motel.feature.complaint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.common.utils.popFragmentWithSlide
import com.app.motel.common.utils.showToast
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Complaint
import com.app.motel.data.model.Status
import com.app.motel.databinding.FragmentComplaintFormBinding
import com.app.motel.feature.complaint.viewmodel.ComplaintViewModel
import com.google.gson.Gson
import javax.inject.Inject

class ComplaintFormFragment: AppBaseFragment<FragmentComplaintFormBinding>() {
    companion object{
        const val KEY_COMPLAINT = "KEY_COMPLAINT"
    }

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentComplaintFormBinding {
        return FragmentComplaintFormBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val viewmodel: ComplaintViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(ComplaintViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        initUI()
        listenStateViewmodel()
    }

    //Trích xuất đối tượng Complaint từ arguments (nếu có)
    //Khởi tạo form với dữ liệu khiếu nại
    //Thiết lập sự kiện nhấn nút Save để tạo khiếu nại mới
    private fun initUI() {
        val complaint: Complaint? = arguments?.getString(KEY_COMPLAINT)?.let {
            Gson().fromJson(it, Complaint::class.java)
        }
        viewmodel.initForm(complaint)
        views.btnSave.setOnClickListener {
            viewmodel.addComplaint(views.txtTitle.text.toString(), views.txtContent.text.toString())
        }
    }

    lateinit var adapter: ComplaintAdapter

    //Quan sát currentComplain để hiển thị dữ liệu khiếu nại
    //Nếu currentComplain là null: hiển thị nút Save và cho phép chỉnh sửa (chế độ tạo mới)
    //Nếu currentComplain không null: ẩn nút Save và vô hiệu hóa trường input (chế độ xem)
    //Quan sát updateComplaint để xử lý kết quả tạo mới
    //Thành công: hiển thị thông báo và quay lại màn hình trước
    //Lỗi: hiển thị thông báo lỗi
    private fun listenStateViewmodel() {
        viewmodel.liveData.currentComplain.observe(viewLifecycleOwner){
            it?.title.apply {
                views.txtTitle.setText(this)
            }
            it?.content?.apply {
                views.txtContent.setText(this)
            }
            views.btnSave.isVisible = it == null
            views.txtTitle.isEnabled = it == null
            views.txtContent.isEnabled = it == null
        }
        viewmodel.liveData.updateComplaint.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    requireActivity().showToast("Tạo đơn khiếu nại thành công")
                    popFragmentWithSlide()
                }
                Status.ERROR -> {
                    requireActivity().showToast(it.message ?: "Tạo đơn khiếu nại thất bại")
                }
                else -> {}
            }
        }
    }

    //Xóa dữ liệu form khi fragment bị hủy
    override fun onDestroy() {
        super.onDestroy()
        viewmodel.clearForm()
    }
}