package com.app.motel.feature.service

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.ultis.navigateFragmentWithSlide
import com.app.motel.common.ultis.observe
import com.app.motel.common.ultis.showToast
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Service
import com.app.motel.data.model.Status
import com.app.motel.databinding.FragmentServiceListBinding
import com.app.motel.feature.service.viewmodel.ServiceViewModel
import com.google.gson.Gson
import javax.inject.Inject

class ServiceListFragment @Inject constructor() : AppBaseFragment<FragmentServiceListBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentServiceListBinding {
        return FragmentServiceListBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : ServiceViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(ServiceViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        init()
        listenStateViewModel()
        super.onViewCreated(view, savedInstanceState)
    }

    private lateinit var adapter: ServiceAdapter

    private fun init() {
        viewModel.getService()

        adapter = ServiceAdapter(object: AppBaseAdapter.AppListener<Service>(){
            override fun onClickItem(item: Service, action: AppBaseAdapter.ItemAction) {
                when(action){
                    AppBaseAdapter.ItemAction.CLICK -> {
                        navigateFragmentWithSlide(
                            R.id.serviceFormFragment,
                            args = Bundle().apply {
                                putString(ServiceFormFragment.ITEM_KEY, Gson().toJson(item))
                            }
                        )
                    }
                    AppBaseAdapter.ItemAction.DELETE -> {
                        viewModel.deleteService(item)
                    }
                    else -> {}
                }

            }
        })
        views.rcv.adapter = adapter

        views.btnAdd.setOnClickListener{
            navigateFragmentWithSlide(R.id.serviceFormFragment)
        }
    }

    private fun listenStateViewModel() {
        viewModel.liveData.services.observe(viewLifecycleOwner){
            val services = viewModel.liveData.getServices
            views.tvEmpty.isVisible = services.isEmpty()
            if(it.isSuccess()){
                adapter.updateData(services)
                Log.d("TAG", "listenStateViewModel: ${services}")
            }
        }

        viewModel.liveData.createService.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    requireContext().showToast(it.message ?: "Thao tác dịch vụ thành công")
                    viewModel.getService()
                }
                Status.ERROR -> {
                    requireContext().showToast(it.message ?: "Có lỗi xảy ra")
                }
                else -> {}
            }
        }
    }
}