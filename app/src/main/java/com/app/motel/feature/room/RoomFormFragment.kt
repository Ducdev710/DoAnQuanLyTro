package com.app.motel.feature.room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.utils.navigateFragmentWithSlide
import com.app.motel.common.utils.popFragmentWithSlide
import com.app.motel.common.utils.showToast
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Resource
import com.app.motel.databinding.FragmentRoomFormBinding
import com.app.motel.feature.room.viewmodel.RoomViewModel
import com.app.motel.feature.service.ServiceFormFragment
import javax.inject.Inject

class RoomFormFragment @Inject constructor() : AppBaseFragment<FragmentRoomFormBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRoomFormBinding {
        return FragmentRoomFormBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : RoomViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(RoomViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        super.onViewCreated(view, savedInstanceState)

        views.btnCancel.setOnClickListener{
            popFragmentWithSlide()
        }
        views.btnAddService.setOnClickListener {
            val roomName = views.txtName.text.toString()
            val roomPrice = views.txtPrice.text.toString()
            val area = views.txtArea.text.toString()
            val maxTenant = views.txtMaxTenant.text.toString()
            val note = views.txtNote.text.toString()

            // Kiểm tra thông tin phòng có hợp lệ không
            if (roomName.isBlank() || roomPrice.isBlank()) {
                activity?.showToast("Vui lòng điền tên phòng và giá phòng trước khi thêm dịch vụ")
                return@setOnClickListener
            }

            // Tạo phòng để lấy ID rồi chuyển đến form thêm dịch vụ
            viewModel.createRoom(roomName, area, maxTenant, roomPrice, note)

            // Listen for successful room creation (one-time observer)
            viewModel.liveData.createRoom.observe(viewLifecycleOwner) { result ->
                if (result.isSuccess() && result.data?.id != null) {
                    // Sử dụng removeObservers để tránh gọi nhiều lần
                    viewModel.liveData.createRoom.removeObservers(viewLifecycleOwner)

                    // Truyền ID phòng vừa tạo qua Bundle
                    navigateFragmentWithSlide(R.id.roomServiceFormFragment, args = Bundle().apply {
                        putString(ServiceFormFragment.ROOM_ID_KEY, result.data.id)
                    })

                    // Reset create room state
                    viewModel.liveData.createRoom.postValue(Resource.Initialize())
                }
            }
        }
        views.btnSave.setOnClickListener {
            viewModel.createRoom(
                views.txtName.text.toString(),
                views.txtArea.text.toString(),
                views.txtMaxTenant.text.toString(),
                views.txtPrice.text.toString(),
                views.txtNote.text.toString()
            )
        }

        listenStateViewModel()
    }

    //Quay lại màn hình trước và hiển thị thông báo khi tạo thành công
    //Hiển thị thông báo lỗi khi tạo thất bại
    private fun listenStateViewModel() {
        viewModel.liveData.createRoom.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                popFragmentWithSlide()
                activity?.showToast("Thêm phòng thành công")
            }else if(it.isError()){
                activity?.showToast(it.message ?: "Có lỗi xảy ra")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.liveData.createRoom.postValue(Resource.Initialize())
    }
}