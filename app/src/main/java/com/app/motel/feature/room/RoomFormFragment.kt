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
        views.btnAddService.setOnClickListener{
            navigateFragmentWithSlide(R.id.roomServiceFormFragment)
        }
        views.btnSave.setOnClickListener {
            viewModel.createRoom(
                views.txtName.text.toString(),
                views.txtArea.text.toString(),
                views.txtMaxTenant.text.toString(),
                views.txtPrice.text.toString(),
            )
        }

        listenStateViewModel()
    }

    private fun listenStateViewModel() {
        viewModel.liveData.createRoom.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                popFragmentWithSlide()
                activity?.showToast("Thêm thành công phòng thành công")
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