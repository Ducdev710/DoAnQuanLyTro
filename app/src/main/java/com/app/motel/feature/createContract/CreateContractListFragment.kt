package com.app.motel.feature.createContract

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.utils.navigateFragmentWithSlide
import com.app.motel.common.utils.showToast
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Room
import com.app.motel.databinding.FragmentCreateContractListBinding
import com.app.motel.feature.createContract.viewmodel.CreateContractViewModel
import com.google.gson.Gson
import javax.inject.Inject

class CreateContractListFragment @Inject constructor() : AppBaseFragment<FragmentCreateContractListBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCreateContractListBinding {
        return FragmentCreateContractListBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : CreateContractViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(CreateContractViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        super.onViewCreated(view, savedInstanceState)
        init()
        listenStateViewModel()
    }

    lateinit var adapter: RoomContractAdapter

    //Khởi tạo adapter với xử lý sự kiện khi chọn phòng
    //Khi chọn phòng, chuyển sang màn hình form tạo hợp đồng với dữ liệu phòng đã chọn
    private fun init() {
        viewModel.getRoom()

        adapter = RoomContractAdapter(object: AppBaseAdapter.AppListener<Room>(){
            override fun onClickItem(item: Room, action: AppBaseAdapter.ItemAction) {
                navigateFragmentWithSlide(R.id.creatContractFormFragment, args = Bundle().apply { putString(CreateContractFormFragment.ITEM_KEY, Gson().toJson(item)) })
            }
        })
        views.rcv.adapter = adapter
    }

    //Khi nhận được danh sách, cập nhật adapter với chỉ các phòng còn trống
    //Hiển thị thông báo "Không có phòng" nếu danh sách trống
    //Xử lý trường hợp có phòng được chọn trước
    private fun listenStateViewModel() {
        viewModel.liveData.rooms.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                val rooms = viewModel.liveData.roomsNotRented
                adapter.updateData(rooms)
                views.tvEmpty.isVisible = rooms.isEmpty()

                handleRoomSelected(rooms)
            }
        }
    }

    //Xử lý sự kiện khi có phòng được chọn trước
    //Kiểm tra nếu có ID phòng được chọn trước (từ Intent)
    //Tìm phòng tương ứng trong danh sách phòng trống
    //Nếu tìm thấy, chuyển ngay đến màn hình form tạo hợp đồng
    //Nếu không tìm thấy, hiển thị thông báo lỗi
    private fun handleRoomSelected(rooms: List<Room>) {
        if(viewModel.liveData.currentRoomId != null){
            val item = rooms.firstOrNull{it.id == viewModel.liveData.currentRoomId}
            viewModel.liveData.currentRoomId = null
            if(item != null){
                navigateFragmentWithSlide(R.id.creatContractFormFragment, args = Bundle().apply { putString(CreateContractFormFragment.ITEM_KEY, Gson().toJson(item)) })
            }else{
                requireActivity().showToast("Không tìm thấy phòng")
            }
        }
    }
}