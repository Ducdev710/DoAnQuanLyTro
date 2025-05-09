package com.app.motel.feature.handleContract

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.entity.HopDongEntity
import com.app.motel.data.model.Contract
import com.app.motel.databinding.FragmentContractListBinding
import com.app.motel.feature.handleContract.viewmodel.HandleContractViewModel
import com.app.motel.ui.custom.CustomTabBar
import javax.inject.Inject

class ContractListFragment : AppBaseFragment<FragmentContractListBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentContractListBinding {
        return FragmentContractListBinding.inflate(inflater, container, false)
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
    lateinit var adapter: ContractActiveAdapter

    private fun init() {
        viewModel.getContracts()
        viewModel.setCurrentStateContract(views.tabBar.currentPosition)
        views.tabBar.setOnTabSelectedListener(object: CustomTabBar.OnTabSelectedListener{
            override fun onTabSelected(position: Int) {
                if(position == 0){
                    viewModel.liveData.currentStateActiveContract.postValue(HopDongEntity.ACTIVE)
                }else if(position == 1){
                    viewModel.liveData.currentStateActiveContract.postValue(HopDongEntity.INACTIVE)
                }
            }
        })
        adapter = ContractActiveAdapter(object: AppBaseAdapter.AppListener<Contract>(){
            override fun onClickItem(item: Contract, action: AppBaseAdapter.ItemAction) {
                when(action){
                    AppBaseAdapter.ItemAction.CLICK -> {
                        DetailContractBottomSheet(item).show(
                            parentFragmentManager, DetailContractBottomSheet::class.java.simpleName
                        )
                    }
                    else -> {}
                }
            }
        })
        views.rcv.adapter = adapter
    }

    private fun listenStateViewModel() {
        viewModel.liveData.isAdmin.observe(viewLifecycleOwner){
            views.tabBar.isVisible = it
        }
        viewModel.liveData.contracts.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                val contracts: List<Contract> = if(viewModel.liveData.isAdmin.value == true)
                    viewModel.liveData.getContractToActive else it.data ?: arrayListOf()

                adapter.updateData(contracts)
                views.tvEmpty.isVisible = contracts.isEmpty()
            }
        }
        viewModel.liveData.currentStateActiveContract.observe(viewLifecycleOwner){
            adapter.updateData(viewModel.liveData.getContractToActive)
            views.tvEmpty.isVisible = viewModel.liveData.getContractToActive.isEmpty()
        }
    }
}