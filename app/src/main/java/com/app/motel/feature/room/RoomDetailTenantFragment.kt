package com.app.motel.feature.room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.utils.navigateFragmentWithSlide
import com.app.motel.common.utils.showDialogConfirm
import com.app.motel.common.utils.showToast
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Tenant
import com.app.motel.databinding.FragmentRoomDetailTenantBinding
import com.app.motel.feature.createContract.CreateContractFormFragment
import com.app.motel.feature.room.viewmodel.RoomViewModel
import com.app.motel.feature.tenant.TenantAdapter
import com.app.motel.feature.tenant.TenantFormFragment
import com.app.motel.feature.tenant.TenantListAddRoomFragment
import com.app.motel.feature.tenant.viewmodel.TenantViewModel
import com.google.gson.Gson
import javax.inject.Inject

class RoomDetailTenantFragment @Inject constructor() : AppBaseFragment<FragmentRoomDetailTenantBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRoomDetailTenantBinding {
        return FragmentRoomDetailTenantBinding.inflate(inflater, container, false)
    }

    private var enableForm: Boolean = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val viewModel : RoomViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(RoomViewModel::class.java)
    }

    private val tenantViewModel : TenantViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(TenantViewModel::class.java)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        super.onViewCreated(view, savedInstanceState)
        listenStateViewModel()

    }

    private fun initUI() {

        views.btnAddTenant.isVisible = enableForm
        views.btnCreateContract.isVisible = enableForm

        views.btnCreateContract.setOnClickListener{
            if(!enableForm) return@setOnClickListener
            navigateFragmentWithSlide(R.id.roomCreateContractFormFragment, args = Bundle().apply { putString(
                CreateContractFormFragment.ITEM_KEY, Gson().toJson(viewModel.liveData.currentRoom.value?.data)) })
        }
        views.btnAddTenant.setOnClickListener{
            if(!enableForm) return@setOnClickListener
            navigateFragmentWithSlide(R.id.tenantListAddRoomFragment, args = Bundle().apply { putString(
                TenantListAddRoomFragment.ITEM_KEY, Gson().toJson(viewModel.liveData.currentRoom.value?.data)) })
        }

        views.rcvTenant.adapter = adapter
    }

    private var adapter = TenantAdapter(
        object : AppBaseAdapter.AppListener<Tenant>() {
            override fun onClickItem(item: Tenant, action: AppBaseAdapter.ItemAction) {
                when(action){
                    AppBaseAdapter.ItemAction.CLICK -> {
                        if(!enableForm) return
                        val json = Gson().toJson(item)
                        navigateFragmentWithSlide(R.id.roomTenantFormFragment, args = Bundle().apply { putString(
                            TenantFormFragment.ITEM_KEY, json) })
                    }
                    AppBaseAdapter.ItemAction.LONG_CLICK -> {
                        // Check if tenant is a contract holder
                        if (item.isContractHolder) {
                            requireActivity().showDialogConfirm(
                                title = "Không thể xóa chủ hợp đồng",
                                content = "Người thuê ${item.fullName} là chủ hợp đồng. Không thể xóa.",
                                confirm = {}
                            )
                        } else {
                            requireActivity().showDialogConfirm(
                                title = "Xác nhận xóa người thuê",
                                content = "Bạn có chắc muốn xóa người thuê ${item.fullName} khỏi phòng ${item.room?.roomName}",
                                confirm = {
                                    tenantViewModel.updateTenantRent(tenant = item, null)
                                }
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    )
    private fun listenStateViewModel() {
        viewModel.userController.state.currentUser.observe(viewLifecycleOwner){
            enableForm = it.data?.isAdmin == true
            initUI()
        }
        viewModel.liveData.currentRoom.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                views.lyEmpty.isVisible = it.data?.contract == null
                views.lyTenant.isVisible = it.data?.contract != null
                adapter.updateData(it.data?.tenants ?: arrayListOf())
                views.btnAddTenant.isVisible = it.data?.maxOccupants == null || it.data.maxOccupants > (it.data.tenants?.size ?: 0)
            }
        }

        tenantViewModel.liveData.updateTenant.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                viewModel.initRoomDetail(viewModel.liveData.currentRoom.value?.data)
                requireContext().showToast(it.message ?: "Thành công")
            }else if(it.isError()){
                requireContext().showToast(it.message ?: "Có lỗi xảy ra")
            }
            tenantViewModel.liveData.updateTenant.postValue(Resource.Initialize())
        }
    }

}