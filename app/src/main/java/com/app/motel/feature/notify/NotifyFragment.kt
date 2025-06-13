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
import com.app.motel.common.utils.showDialogConfirm
import com.app.motel.common.utils.startActivityWithSlide
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.entity.KhieuNaiEntity
import com.app.motel.data.model.Complaint
import com.app.motel.data.model.Notification
import com.app.motel.databinding.FragmentNotifyBinding
import com.app.motel.feature.createContract.CreateContractActivity
import com.app.motel.feature.home.viewmodel.HomeViewModel
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

        // Đánh dấu thông báo là đã đọc khi người thuê xem màn hình thông báo
        if (!viewModel.liveData.isAdmin) {
            val homeViewModel = ViewModelProvider(requireActivity(), viewModelFactory)
                .get(HomeViewModel::class.java)
            homeViewModel.markAllNotificationsAsRead()
            Log.d("NotifyFragment", "Đánh dấu tất cả thông báo đã đọc cho người thuê")
        }
    }

    val adapterNotificationAdmin = NotificationAdminAdapter(object : AppBaseAdapter.AppListener<Complaint>(){
        override fun onClickItem(item: Complaint, action: AppBaseAdapter.ItemAction) {
            when (action) {
                AppBaseAdapter.ItemAction.LONG_CLICK -> {
                    // Hiển thị dialog xử lý khiếu nại
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
                AppBaseAdapter.ItemAction.UPDATE_STATUS -> {
                    // Cập nhật trạng thái cho thông báo kiểu APPLICATION
                    if (item.type == KhieuNaiEntity.Type.APPLICATION.value) {
                        viewModel.updateStateComplaint(item, KhieuNaiEntity.Status.RESOLVED.value)
                    }
                    return
                }
                else -> {
                    // Xử lý yêu cầu thuê phòng
                    when(item.type){
                        KhieuNaiEntity.Type.RENT_ROOM.value -> {
                            viewModel.setCurrentHandleComplaint(item)
                            requireActivity().startActivityWithSlide(Intent(requireActivity(), CreateContractActivity::class.java).apply {
                                putExtra(CreateContractActivity.KEY_ROOM_ID, item.roomId)
                                putExtra(CreateContractActivity.KEY_TENANT_ID, item.submittedBy)
                            }, launcher)
                        }
                        KhieuNaiEntity.Type.APPLICATION.value -> {
                            // Nếu là thông báo APPLICATION và đang ở trạng thái "Mới", tự động đánh dấu đã đọc
                            if (item.status == KhieuNaiEntity.Status.NEW.value) {
                                viewModel.updateStateComplaint(item, KhieuNaiEntity.Status.RESOLVED.value)
                            }
                        }
                    }
                }
            }
        }
    })

    val adapterNotificationUser = NotificationUserAdapter(object : AppBaseAdapter.AppListener<Notification>(){
        override fun onClickItem(item: Notification, action: AppBaseAdapter.ItemAction) {
            // Đánh dấu thông báo cụ thể đã đọc khi người thuê click vào
            if (!viewModel.liveData.isAdmin && item.isRead != 1) {
                viewModel.markNotificationAsRead(item.id)
            }
        }
    })

    //Cập nhật trạng thái khiếu nại sau khi tạo hợp đồng
    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val currentHandleComplaint = viewModel.liveData.currentHandleComplaint.value
        currentHandleComplaint?.apply{
            val status = if (result.resultCode == Activity.RESULT_OK) KhieuNaiEntity.Status.RESOLVED.value
            else KhieuNaiEntity.Status.PENDING.value
            viewModel.updateStateComplaint(this, status)
        }

        viewModel.setCurrentHandleComplaint(null)
    }

    //Xác định vai trò và thiết lập UI phù hợp
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

            // Đánh dấu đã đọc tất cả thông báo ứng dụng khi chọn tab APPLICATION
            if (viewModel.liveData.currentTabType.value == KhieuNaiEntity.Type.APPLICATION) {
                markAllApplicationNotificationsAsRead()
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

    // Phương thức mới để đánh dấu đã đọc tất cả thông báo ứng dụng
    private fun markAllApplicationNotificationsAsRead() {
        val complaints = viewModel.liveData.getNotifyAdmin
        val appNotifications = complaints.filter {
            it.type == KhieuNaiEntity.Type.APPLICATION.value &&
                    it.status == KhieuNaiEntity.Status.NEW.value
        }

        if (appNotifications.isNotEmpty()) {
            viewModel.updateAllApplicationNotifications()
        }
    }

    private fun listenStateViewModel() {
        viewModel.userController.state.currentUser.observe(viewLifecycleOwner){
            val isAdmin = it.data?.isAdmin == true
            initUI(isAdmin)

            // Đánh dấu thông báo là đã đọc khi người thuê xem màn hình thông báo
            if (!isAdmin) {
                val homeViewModel = ViewModelProvider(requireActivity(), viewModelFactory)
                    .get(HomeViewModel::class.java)
                homeViewModel.markAllNotificationsAsRead()
            }
        }

        // Dữ liệu chủ nhà trọ
        viewModel.liveData.complaints.observe(viewLifecycleOwner){
            if(!viewModel.liveData.isAdmin) return@observe
            val complaints = viewModel.liveData.getNotifyAdmin
            adapterNotificationAdmin.updateData(complaints)
            views.tvEmpty.isVisible = complaints.isEmpty()

            // Kiểm tra và đánh dấu đã đọc tất cả thông báo ứng dụng nếu đang ở tab APPLICATION
            if (viewModel.liveData.currentTabType.value == KhieuNaiEntity.Type.APPLICATION) {
                markAllApplicationNotificationsAsRead()
            }
        }

        viewModel.liveData.currentTabType.observe(viewLifecycleOwner){
            if(!viewModel.liveData.isAdmin) return@observe
            Log.e("NotifyFragment", "complaints: ${viewModel.liveData.complaints.value}")
            val complaints = viewModel.liveData.getNotifyAdmin
            adapterNotificationAdmin.updateData(complaints)
            views.tvEmpty.isVisible = complaints.isEmpty()

            // Nếu chuyển sang tab APPLICATION, tự động đánh dấu đã đọc tất cả thông báo ứng dụng
            if (it == KhieuNaiEntity.Type.APPLICATION) {
                markAllApplicationNotificationsAsRead()
            }
        }

        // Dữ liệu người thuê
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