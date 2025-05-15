package com.app.motel.feature.boardingHouse

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.common.utils.showDialogConfirm
import com.app.motel.common.utils.showToast
import com.app.motel.common.utils.startActivityWithTransition
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.Status
import com.app.motel.databinding.FragmentBoardingHouseListBinding
import com.app.motel.feature.boardingHouse.viewmodel.BoardingHouseViewModel
import com.app.motel.feature.profile.UserController
import com.google.gson.Gson
import javax.inject.Inject

class BoardingHouseListFragment @Inject constructor() : AppBaseFragment<FragmentBoardingHouseListBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentBoardingHouseListBinding {
        return FragmentBoardingHouseListBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var profileViewModel : UserController

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : BoardingHouseViewModel by lazy{
        ViewModelProvider(this, viewModelFactory).get(BoardingHouseViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getBoardingHouse()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        init()
        listenStateViewmodel()
    }

    val adapter = BoardingHouseAdapter(object : AppBaseAdapter.AppListener<BoardingHouse>() {
        override fun onClickItem(item: BoardingHouse, action: AppBaseAdapter.ItemAction) {
            when (action) {
                AppBaseAdapter.ItemAction.CLICK -> {
                    profileViewModel.setCurrentBoardingHouse(item)
                }
                AppBaseAdapter.ItemAction.EDIT -> {
                    requireActivity().startActivityWithTransition(Intent(requireActivity(), BoardingHouseActivity::class.java).apply {
                        putExtra(BoardingHouseActivity.KEY_BOARDING_HOUSE, Gson().toJson(item))
                    })
                }
                AppBaseAdapter.ItemAction.DELETE -> {
                    requireContext().showDialogConfirm(
                        title = "Bạn có muốn xóa khu trọ này",
                        content = "Tất cả dữ liệu liên quan đến khu trọ này sẽ bị xóa",
                        confirm = {
                            viewModel.deleteBoardingHouse(item)
                        }
                    )
                }
                else -> {}
            }
        }
    })
    private fun init() {

        views.rcv.adapter = adapter

        views.btnAdd.setOnClickListener {
            requireActivity().startActivityWithTransition(Intent(requireActivity(), BoardingHouseActivity::class.java))
        }
    }

    private fun listenStateViewmodel() {

        profileViewModel.state.currentUser.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    val user = it.data!!
                    views.tvUserName.text = user.name
                    views.tvNumberPhone.text = user.phone
                }
                else -> {}
            }
        }
        profileViewModel.state.currentBoardingHouse.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    adapter.updateCurrentBoardingHouse(it.data)
                }
                else -> {}
            }
        }
        viewModel.liveData.boardingHouse.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    val boardingHouse = it.data ?: arrayListOf()
                    adapter.updateData(boardingHouse)
                }
                else -> {}
            }
        }
        viewModel.liveData.saveBoardingHouse.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    viewModel.getBoardingHouse()
                    requireContext().showToast(it.message ?: "Thành công")
                }
                Status.ERROR -> {
                    requireContext().showToast(it.message ?: "Có lỗi xảy ra")
                }
                else -> {}
            }
        }
    }
}