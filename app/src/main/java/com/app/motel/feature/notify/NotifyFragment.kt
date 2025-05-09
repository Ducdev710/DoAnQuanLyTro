package com.app.motel.feature.notify

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.common.ultis.showDialogConfirm
import com.app.motel.common.ultis.startActivityWithSlide
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.entity.KhieuNaiEntity
import com.app.motel.data.model.Complaint
import com.app.motel.data.model.Notification
import com.app.motel.databinding.FragmentNotifyBinding
import com.app.motel.feature.createContract.CreateContractActivity
import com.app.motel.feature.notify.viewmodel.NotifyViewModel
import com.app.motel.ui.custom.CustomTabBar
import javax.inject.Inject

class NotifyFragment @Inject constructor() : AppBaseFragment<FragmentNotifyBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNotifyBinding {
        return FragmentNotifyBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : NotifyViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(NotifyViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        super.onViewCreated(view, savedInstanceState)
        listenStateViewModel()
    }

    val adapterNotificationAdmin = NotificationAdminAdapter(object : AppBaseAdapter.AppListener<Complaint>(){
        override fun onClickItem(item: Complaint, action: AppBaseAdapter.ItemAction) {
            if(action == AppBaseAdapter.ItemAction.LONG_CLICK){
                requireActivity().showDialogConfirm(
                    title = "Cập nhật trạng thái",
                    content = "${item.content}",
                    buttonCancel = "Từ chối",
                    buttonConfirm = "Hoàn thành",
                    cancel = {
                        viewModel.updateStateComplaint(item, KhieuNaiEntity.Status.REJECTED.value)
                    },
                    confirm = {
                        viewModel.updateStateComplaint(item, KhieuNaiEntity.Status.RESOLVED.value)
                    }
                )
                return
            }
            when(item.type){
                KhieuNaiEntity.Type.RENT_ROOM.value -> {
                    viewModel.setCurrentHandleComplaint(item)
                    requireActivity().startActivityWithSlide(Intent(requireActivity(), CreateContractActivity::class.java).apply {
                        putExtra(CreateContractActivity.KEY_ROOM_ID, item.roomId)
                        putExtra(CreateContractActivity.KEY_TENANT_ID, item.submittedBy)
                    },
                        launcher)
                }
            }
        }
    })

    val adapterNotificationUser = NotificationUserAdapter(object : AppBaseAdapter.AppListener<Notification>(){
        override fun onClickItem(item: Notification, action: AppBaseAdapter.ItemAction) {
//            if(action == AppBaseAdapter.ItemAction.LONG_CLICK){
//
//            }
        }
    })

    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        val currentHandleComplaint = viewModel.liveData.currentHandleComplaint.value
        currentHandleComplaint?.apply{
            val status = if (result.resultCode == Activity.RESULT_OK) KhieuNaiEntity.Status.RESOLVED.value
                else KhieuNaiEntity.Status.PENDING.value
            viewModel.updateStateComplaint(this, status)
        }

        viewModel.setCurrentHandleComplaint(null)
    }

    private fun initUI(isAdmin: Boolean) {
        viewModel.liveData.isAdmin = isAdmin
        if (isAdmin){
            views.tabBar.setTabs(arrayListOf("THÔNG BÁO ỨNG DỤNG", "KHIẾU NẠI TỪ KHÁCH HÀNG", "YÊU CẦU THUÊ PHÒNG"))

            viewModel.getNotificationAdmin()
            views.rcv.adapter = adapterNotificationAdmin
            views.tabBar.setOnTabSelectedListener(object: CustomTabBar.OnTabSelectedListener{
                override fun onTabSelected(position: Int) {
                    viewModel.setCurrentType(position)
                }
            })
            views.tabBar.post {
                views.tabBar.setTabSelected(when(viewModel.liveData.currentTabType.value){
                    KhieuNaiEntity.Type.APPLICATION -> 0
                    KhieuNaiEntity.Type.COMPLAINT -> 1
                    KhieuNaiEntity.Type.RENT_ROOM -> 2
                    else -> 0
                })
            }
            return
        }

        views.tabBar.setTabs(arrayListOf("THÔNG BÁO CHUNG", "THÔNG BÁO TỪ CHỦ NHÀ"))
        viewModel.getNotificationUser()
        views.rcv.adapter = adapterNotificationUser
        views.tabBar.setOnTabSelectedListener(object: CustomTabBar.OnTabSelectedListener{
            override fun onTabSelected(position: Int) {
                viewModel.setCurrentType(position)
            }
        })
        views.tabBar.post {
            views.tabBar.setTabSelected(when(viewModel.liveData.currentTabGeneral.value){
                true -> 0
                false -> 1
                else -> 0
            })
        }

    }

    private fun listenStateViewModel() {
        viewModel.userController.state.currentUser.observe(viewLifecycleOwner){
            val isAdmin = it.data?.isAdmin == true
            initUI(isAdmin)
        }

        // data admin
        viewModel.liveData.complaints.observe(viewLifecycleOwner){
            if(!viewModel.liveData.isAdmin) return@observe
            val complaints = viewModel.liveData.getNotifyAdmin
            adapterNotificationAdmin.updateData(complaints)
            views.tvEmpty.isVisible = complaints.isEmpty()
        }
        viewModel.liveData.currentTabType.observe(viewLifecycleOwner){
            if(!viewModel.liveData.isAdmin) return@observe
            Log.e("NotifyFragment", "complaints: ${viewModel.liveData.complaints.value}")
            val complaints = viewModel.liveData.getNotifyAdmin
            adapterNotificationAdmin.updateData(complaints)
            views.tvEmpty.isVisible = complaints.isEmpty()
        }

        // data user
        viewModel.liveData.notifications.observe(viewLifecycleOwner){
            if(viewModel.liveData.isAdmin) return@observe
            val notifications = viewModel.liveData.getNotifyUser
            adapterNotificationUser.updateData(notifications)
            views.tvEmpty.isVisible = notifications.isEmpty()
        }
        viewModel.liveData.currentTabGeneral.observe(viewLifecycleOwner){
            if(viewModel.liveData.isAdmin) return@observe
            val notifications = viewModel.liveData.getNotifyUser
            adapterNotificationUser.updateData(notifications)
            views.tvEmpty.isVisible = notifications.isEmpty()
        }
    }

}