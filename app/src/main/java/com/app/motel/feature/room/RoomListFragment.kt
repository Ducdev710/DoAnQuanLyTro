package com.app.motel.feature.room

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.ultis.navigateFragmentWithSlide
import com.app.motel.common.ultis.observe
import com.app.motel.common.ultis.showDialogConfirm
import com.app.motel.common.ultis.showToast
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.entity.PhongEntity
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.model.Tenant
import com.app.motel.databinding.FragmentListRoomBinding
import com.app.motel.feature.room.viewmodel.RoomViewModel
import com.app.motel.ui.custom.CustomTabBar
import com.google.gson.Gson
import javax.inject.Inject

class RoomListFragment @Inject constructor() : AppBaseFragment<FragmentListRoomBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentListRoomBinding {
        return FragmentListRoomBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : RoomViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(RoomViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        super.onViewCreated(view, savedInstanceState)
        init()
        listenStateViewModel()
    }

    lateinit var adapter: RoomAdapter

    private fun init() {
        adapter = RoomAdapter(object: AppBaseAdapter.AppListener<Room>(){
            override fun onClickItem(item: Room, action: AppBaseAdapter.ItemAction) {
                when(action){
                    AppBaseAdapter.ItemAction.CLICK -> {
                        navigateFragmentWithSlide(R.id.roomDetailFragment, args = Bundle().apply { putString(RoomDetailFragment.ITEM_KEY, Gson().toJson(item)) })
                    }
                    AppBaseAdapter.ItemAction.CUSTOM -> {
                        requireActivity().showDialogConfirm(
                            title = "XÁC NHẬN THUÊ PHÒNG",
                            content = "Bạn có chắc muốn thuê phòng ${item.roomName}",
                            confirm = {
                                viewModel.rentRoom(item)
                            }
                        )
                    }
                    else -> {
                    }
                }
            }
        })
        views.rcv.adapter = adapter
        views.txtSearch.setText(viewModel.liveData.searchText.value ?: "")
        views.tabBar.setOnTabSelectedListener(object: CustomTabBar.OnTabSelectedListener{
            override fun onTabSelected(position: Int) {
                when(position){
                    0 -> viewModel.setStateRoomListData(null)
                    1 -> viewModel.setStateRoomListData(PhongEntity.Status.RENTED)
                    2 -> viewModel.setStateRoomListData(PhongEntity.Status.EMPTY)
                }
            }
        })
        views.tabBar.post {
            views.tabBar.setTabSelected(when(viewModel.liveData.currentRoomState.value?.data){
                PhongEntity.Status.RENTED -> 1
                PhongEntity.Status.EMPTY -> 2
                else -> 0
            })
        }
        views.txtSearch.addTextChangedListener {
            viewModel.liveData.searchText.postValue(views.txtSearch.text?.toString())
        }
        views.btnAdd.setOnClickListener{
            navigateFragmentWithSlide(R.id.roomFormFragment,)
        }
    }

    private fun listenStateViewModel() {
        viewModel.userController.state.currentUser.observe(viewLifecycleOwner) { userResource ->
            val currentUser = userResource.data
            views.tabBar.isVisible = currentUser?.isAdmin == true
            views.btnAdd.isVisible = currentUser?.isAdmin == true
        }

        viewModel.liveData.currentRoomState.observe(viewLifecycleOwner) {
            Log.e("RoomListFragment", "currentRoomState: ${it.data}")
            if (it.isSuccess()) {
                val currentUser = viewModel.userController.state.currentUser.value?.data
                val roomState = it.data

                // If user is a tenant (not admin) and viewing empty rooms
                if (currentUser != null && !currentUser.isAdmin && roomState == PhongEntity.Status.EMPTY) {
                    currentUser.child?.let { child ->
                        if (child is Tenant && child.landlordId != null) {
                            // Load only empty rooms from tenant's landlord
                            viewModel.loadRoomsByLandlordId(child.landlordId)
                            return@observe
                        }
                    }
                }

                // For all other cases (admin users or tenant viewing their rented rooms)
                viewModel.getRoom()
            }
        }

        viewModel.liveData.rooms.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                val isShowRentButton = viewModel.liveData.currentRoomState.value?.data == PhongEntity.Status.EMPTY
                        && viewModel.userController.state.currentUser.value?.data?.isAdmin == false

                val rooms = viewModel.liveData.roomsWithCurrentStateSearch
                adapter.updateData(rooms)
                adapter.setShowButtonRentRoom(isShowRentButton)

                views.tvEmpty.isVisible = rooms.isEmpty()
            }
        }

        viewModel.liveData.searchText.observe(viewLifecycleOwner){
            val rooms = viewModel.liveData.roomsWithCurrentStateSearch
            adapter.updateData(rooms)
            views.tvEmpty.isVisible = rooms.isEmpty()
        }

        viewModel.liveData.rentRoom.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                requireActivity().showToast("Yêu cầu thuê phòng thành công")
                viewModel.getRoom()
            }else if(it.isError()){
                requireActivity().showToast(it.message ?: "Có lỗi xảy ra")
            }
            viewModel.liveData.rentRoom.postValue(Resource.Initialize())
        }
    }
}