package com.app.motel.feature.tenant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.utils.navigateFragmentWithSlide
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.entity.NguoiThueEntity
import com.app.motel.data.model.Tenant
import com.app.motel.databinding.FragmentTenantListBinding
import com.app.motel.feature.tenant.viewmodel.TenantViewModel
import com.app.motel.ui.custom.CustomTabBar
import com.google.gson.Gson
import javax.inject.Inject

class TenantListFragment : AppBaseFragment<FragmentTenantListBinding>() {

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTenantListBinding {
        return FragmentTenantListBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : TenantViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(TenantViewModel::class.java)
    }

    lateinit var adapter: TenantAdapter

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
                navigateFragmentWithSlide(R.id.tenantFormFragment, args = Bundle().apply { putString(
                    TenantFormFragment.ITEM_KEY, Gson().toJson(item)) })
            }
        })
        views.rcv.adapter = adapter
        views.txtSearch.setText(viewModel.liveData.searchText.value ?: "")
        views.tabBar.setOnTabSelectedListener(object: CustomTabBar.OnTabSelectedListener{
            override fun onTabSelected(position: Int) {
                when(position){
                    0 -> viewModel.liveData.filterState.postValue(NguoiThueEntity.Status.ACTIVE)
                    1 -> viewModel.liveData.filterState.postValue(NguoiThueEntity.Status.INACTIVE)
                    2 -> viewModel.liveData.filterState.postValue(NguoiThueEntity.Status.TEMPORARY_ABSENT)
                }
            }
        })
        views.tabBar.post {
            views.tabBar.setTabSelected(when(viewModel.liveData.filterState.value){
                NguoiThueEntity.Status.ACTIVE -> 0
                NguoiThueEntity.Status.INACTIVE -> 1
                NguoiThueEntity.Status.TEMPORARY_ABSENT -> 2
                else -> 0
            })
        }
        views.txtSearch.addTextChangedListener {
            viewModel.liveData.searchText.postValue(views.txtSearch.text?.toString())
        }
        views.btnAdd.setOnClickListener{
            navigateFragmentWithSlide(R.id.tenantFormFragment,)
        }
    }


    private fun listenStateViewModel() {
        viewModel.liveData.tenants.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                val tenants = viewModel.liveData.getListTenantByStateSearch
                adapter.updateData(tenants)
                views.tvEmpty.isVisible = tenants.isEmpty()
            }
        }
        viewModel.liveData.filterState.observe(viewLifecycleOwner){
            val tenants = viewModel.liveData.getListTenantByStateSearch
            adapter.updateData(tenants)
            views.tvEmpty.isVisible = tenants.isEmpty()
        }
        viewModel.liveData.searchText.observe(viewLifecycleOwner){
            val tenants = viewModel.liveData.getListTenantByStateSearch
            adapter.updateData(tenants)
            views.tvEmpty.isVisible = tenants.isEmpty()
        }
    }

}