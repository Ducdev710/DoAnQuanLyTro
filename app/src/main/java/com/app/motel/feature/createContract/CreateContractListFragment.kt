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

    private fun init() {
        viewModel.getRoom()

        adapter = RoomContractAdapter(object: AppBaseAdapter.AppListener<Room>(){
            override fun onClickItem(item: Room, action: AppBaseAdapter.ItemAction) {
                navigateFragmentWithSlide(R.id.creatContractFormFragment, args = Bundle().apply { putString(CreateContractFormFragment.ITEM_KEY, Gson().toJson(item)) })
            }
        })
        views.rcv.adapter = adapter
    }

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