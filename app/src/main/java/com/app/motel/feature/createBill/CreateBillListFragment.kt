package com.app.motel.feature.createBill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.ultis.navigateFragmentWithSlide
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Room
import com.app.motel.databinding.FragmentCreateBillListBinding
import com.app.motel.feature.createBill.viewmodel.CreateBillViewModel
import com.google.gson.Gson
import javax.inject.Inject

class CreateBillListFragment @Inject constructor() : AppBaseFragment<FragmentCreateBillListBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCreateBillListBinding {
        return FragmentCreateBillListBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : CreateBillViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(CreateBillViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        super.onViewCreated(view, savedInstanceState)
        init()
        listenStateViewModel()
    }

    lateinit var adapter: RoomBillAdapter

    private fun init() {
        viewModel.getService()

        adapter = RoomBillAdapter(object: AppBaseAdapter.AppListener<Room>(){
            override fun onClickItem(item: Room, action: AppBaseAdapter.ItemAction) {
                navigateFragmentWithSlide(R.id.createBillFormFragment, args = Bundle().apply { putString(CreateBillFormFragment.ITEM_KEY, Gson().toJson(item)) })
            }
        })
        views.rcv.adapter = adapter
    }

    private fun listenStateViewModel() {
        viewModel.liveData.rooms.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                val rooms = viewModel.liveData.roomsRented
                adapter.updateData(rooms)
                views.tvEmpty.isVisible = rooms.isEmpty()
            }
        }
    }
}