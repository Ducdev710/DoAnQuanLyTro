package com.app.motel.feature.tenant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.common.utils.popFragmentWithSlide
import com.app.motel.common.utils.showToast
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.entity.NguoiThueEntity
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.model.Tenant
import com.app.motel.databinding.FragmentTenantListAddRoomBinding
import com.app.motel.feature.tenant.viewmodel.TenantViewModel
import com.google.gson.Gson
import javax.inject.Inject

class TenantListAddRoomFragment : AppBaseFragment<FragmentTenantListAddRoomBinding>() {

    companion object {
        val ITEM_KEY: String = "ROOM_KEY"
    }
    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTenantListAddRoomBinding {
        return FragmentTenantListAddRoomBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : TenantViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(TenantViewModel::class.java)
    }

    lateinit var adapter: TenantAdapter

    val currentRoomToAdd: Room? get() = Gson().fromJson(arguments?.getString(ITEM_KEY), Room::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        viewModel.liveData

        init()
        listenStateViewModel()
    }
    private fun init() {
        viewModel.getTenants()

        adapter = TenantAdapter(object: AppBaseAdapter.AppListener<Tenant>(){
            override fun onClickItem(item: Tenant, action: AppBaseAdapter.ItemAction) {
                viewModel.updateTenantRent(tenant = item, currentRoomToAdd)
            }
        })
        views.rcv.adapter = adapter
    }

    private fun listenStateViewModel() {
        viewModel.liveData.tenants.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                val tenants = (viewModel.liveData.tenants.value?.data ?: arrayListOf()).filter{ tenant ->
                    tenant.status == NguoiThueEntity.Status.INACTIVE.value
                }

                adapter.updateData(tenants)
                views.tvEmpty.isVisible = tenants.isEmpty()
            }
        }
        viewModel.liveData.updateTenant.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                viewModel.getTenants()
                requireContext().showToast(it.message ?: "Thành công")
                popFragmentWithSlide()
            }else if(it.isError()){
                requireContext().showToast(it.message ?: "Có lỗi xảy ra")
            }
            viewModel.liveData.updateTenant.postValue(Resource.Initialize())
        }
    }

}