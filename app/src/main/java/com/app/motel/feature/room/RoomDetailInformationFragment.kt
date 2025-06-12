package com.app.motel.feature.room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.utils.navigateFragmentWithSlide
import com.app.motel.common.utils.popFragmentWithSlide
import com.app.motel.common.utils.showDialogConfirm
import com.app.motel.common.utils.showToast
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.model.Service
import com.app.motel.databinding.FragmentRoomDetailInformationBinding
import com.app.motel.feature.room.viewmodel.RoomViewModel
import com.app.motel.feature.service.ServiceFormFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import javax.inject.Inject

class RoomDetailInformationFragment @Inject constructor() : AppBaseFragment<FragmentRoomDetailInformationBinding>() {

    private var enableForm: Boolean = false
    private var isAdmin: Boolean = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val viewModel : RoomViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(RoomViewModel::class.java)
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRoomDetailInformationBinding {
        return FragmentRoomDetailInformationBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        super.onViewCreated(view, savedInstanceState)

        listenStateViewModel()
        initUI()
    }

    private fun initUI() {
        setEnableEdittext(views.txtNameRoom, enableForm)
        setEnableEdittext(views.txtArea, enableForm)
        setEnableEdittext(views.txtMaxTenant, enableForm)
        setEnableEdittext(views.txtCurrentTenant, false)
        setEnableEdittext(views.txtPriceRoom, enableForm)
        setEnableEdittext(views.txtNote, enableForm)
        setEnableEdittext(views.txtCountService, false)

        // Chỉ hiển thị ghi chú sửa chữa nếu người dùng là chủ nhà
        if (isAdmin) {
            views.repairNotesContainer.visibility = View.VISIBLE
            setEnableEdittext(views.txtRepairNote, enableForm)
        } else {
            views.repairNotesContainer.visibility = View.GONE
        }
        // Hiển thị/ẩn các nút chức năng
        views.btnChangeService.isVisible = enableForm
        views.btnUpdate.isVisible = enableForm
        views.btnDelete.isVisible = enableForm

        views.btnChangeService.setOnClickListener {
            if (!enableForm) return@setOnClickListener
            navigateFragmentWithSlide(R.id.roomServiceFormFragment, args = Bundle().apply {
                putString(ServiceFormFragment.ROOM_ID_KEY, viewModel.liveData.currentRoom.value?.data?.id)
            })
        }

        views.rcvService.adapter = adapterService
        views.btnUpdate.setOnClickListener {
            if(!enableForm) return@setOnClickListener
            viewModel.updateRoom(
                viewModel.liveData.currentRoom.value?.data,
                views.txtNameRoom.text.toString(),
                views.txtArea.text.toString(),
                views.txtMaxTenant.text.toString(),
                views.txtPriceRoom.text.toString(),
                views.txtNote.text.toString(),
                if (isAdmin) views.txtRepairNote.text.toString() else null
            )
        }

        views.btnDelete.setOnClickListener{
            if(!enableForm) return@setOnClickListener
            requireContext().showDialogConfirm(
                "Xóa phòng",
                "Bạn có chắc muốn xóa phòng ${viewModel.liveData.currentRoom.value?.data?.roomName} không ?",
                confirm = {
                    viewModel.deleteRoom(viewModel.liveData.currentRoom.value?.data)
                }
            )
        }
    }

    //Tạo adapter cho RecyclerView hiển thị danh sách dịch vụ
    //Xử lý sự kiện click để chỉnh sửa dịch vụ
    //Truyền thông tin dịch vụ và ID phòng qua Bundle
    private var adapterService: DetailRoomServiceAdapter = DetailRoomServiceAdapter(
        object : AppBaseAdapter.AppListener<Service>() {
            override fun onClickItem(item: Service, action: AppBaseAdapter.ItemAction) {
                if(!enableForm) return
                navigateFragmentWithSlide(R.id.roomServiceFormFragment, args = Bundle().apply {
                    putString(ServiceFormFragment.ITEM_KEY, Gson().toJson(item))
                    putString(ServiceFormFragment.ROOM_ID_KEY, viewModel.liveData.currentRoom.value?.data?.id)
                })
            }
        }
    )

    private fun listenStateViewModel() {
        viewModel.userController.state.currentUser.observe(viewLifecycleOwner){
            enableForm = it.data?.isAdmin == true
            isAdmin = it.data?.isAdmin == true
            initUI()
        }
        viewModel.liveData.updateRoom.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                requireActivity().showToast("Cập nhật phòng thành công")
            }else if(it.isError()){
                requireActivity().showToast(it.message ?: "Có lỗi xảy ra")
            }
            viewModel.liveData.updateRoom.postValue(Resource.Initialize())
        }
        viewModel.liveData.deleteRoom.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                requireActivity().showToast("Xóa phòng thành công")
                popFragmentWithSlide()
            }else if(it.isError()){
                requireActivity().showToast(it.message ?: "Có lỗi xảy ra")
            }
        }
        viewModel.liveData.currentRoom.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                it.data?.also { currentRoom: Room? ->
                    views.tvNameBoardingHouse.text = currentRoom?.boardingHouse?.name
                    views.tvStatusBoardingHouse.text = currentRoom?.boardingHouse?.address
                    views.txtNameRoom.setText(currentRoom?.roomName)
                    views.txtArea.setText(currentRoom?.area?.toString())
                    views.txtMaxTenant.setText(currentRoom?.maxOccupants?.toString())
                    views.txtCurrentTenant.setText(currentRoom?.tenants?.size?.toString())
                    views.txtPriceRoom.setText(currentRoom?.rentalPrice)
                    views.txtNote.setText(currentRoom?.note)

                    // Chỉ hiển thị và cho phép chỉnh sửa ghi chú sửa chữa nếu người dùng là chủ nhà
                    if (isAdmin) {
                        views.txtRepairNote.setText(currentRoom?.repairNote)
                    }

                    views.txtCountService.setText(currentRoom?.listService?.size?.toString())
                    adapterService.updateData(currentRoom?.listService ?: arrayListOf())
                    views.tvEmpty.isVisible = currentRoom?.listService.isNullOrEmpty()
                }
            }
        }
    }

    private fun setEnableEdittext(txtView: TextInputEditText, enable: Boolean){
        txtView.isEnabled = enable
        txtView.backgroundTintList = resources.getColorStateList(if(enable) R.color.background1 else R.color.background3, requireContext().theme)
    }
}