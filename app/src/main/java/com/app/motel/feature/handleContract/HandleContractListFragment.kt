package com.app.motel.feature.handleContract

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
import com.app.motel.data.entity.PhongEntity
import com.app.motel.data.model.Contract
import com.app.motel.databinding.FragmentHandleContractListBinding
import com.app.motel.feature.handleContract.viewmodel.HandleContractViewModel
import com.app.motel.ui.custom.CustomTabBar
import com.google.gson.Gson
import javax.inject.Inject

class HandleContractListFragment @Inject constructor() : AppBaseFragment<FragmentHandleContractListBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHandleContractListBinding {
        return FragmentHandleContractListBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : HandleContractViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(HandleContractViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        super.onViewCreated(view, savedInstanceState)
        init()
        listenStateViewModel()
    }
    lateinit var adapter: HandleContractAdapter

    private fun init() {
        viewModel.getContracts()
        views.tabBar.setOnTabSelectedListener(object: CustomTabBar.OnTabSelectedListener{
            override fun onTabSelected(position: Int) {
                viewModel.setCurrentStateContract(position)
            }
        })
        views.tabBar.post {
            views.tabBar.setTabSelected(when(viewModel.liveData.currentStateContract.value){
                Contract.State.ACTIVE -> 0
                Contract.State.NEAR_END -> 1
                Contract.State.ENDED -> 2
                else -> 0
            })
        }
        adapter = HandleContractAdapter(object: AppBaseAdapter.AppListener<Contract>(){
            override fun onClickItem(item: Contract, action: AppBaseAdapter.ItemAction) {
                when(action){
                    AppBaseAdapter.ItemAction.CLICK -> {
                        DetailContractBottomSheet(item).show(
                            parentFragmentManager, DetailContractBottomSheet::class.java.simpleName
                        )
                    }
                    AppBaseAdapter.ItemAction.EDIT -> {
                        RefreshContractBottomSheet(item).show(
                            parentFragmentManager, RefreshContractBottomSheet::class.java.simpleName
                        )
                    }
                    AppBaseAdapter.ItemAction.DELETE -> {
                        navigateFragmentWithSlide(R.id.handleContractTerminalFragment, Bundle().apply {
                            putString(HandleContractTerminalFragment.CONTRACT_KEY, Gson().toJson(item))
                        })
                    }
                    else -> {}
                }
            }
        })
        views.rcv.adapter = adapter
    }

    private fun listenStateViewModel() {
        viewModel.liveData.contracts.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                adapter.updateData(viewModel.liveData.getContractToState)
                views.tvEmpty.isVisible = viewModel.liveData.getContractToState.isEmpty()
            }
        }
        viewModel.liveData.currentStateContract.observe(viewLifecycleOwner){
            adapter.updateData(viewModel.liveData.getContractToState)
            views.tvEmpty.isVisible = viewModel.liveData.getContractToState.isEmpty()
        }
    }
}