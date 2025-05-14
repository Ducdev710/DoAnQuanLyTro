package com.app.motel.feature.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.ultis.startActivityWithSlide
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.entity.HoaDonEntity
import com.app.motel.data.entity.PhongEntity
import com.app.motel.databinding.FragmentManagementBoardingHouseBinding
import com.app.motel.feature.complaint.ComplaintActivity
import com.app.motel.feature.createBill.CreateBillActivity
import com.app.motel.feature.createContract.CreateContractActivity
import com.app.motel.feature.handleBill.HandleBillActivity
import com.app.motel.feature.handleContract.ContractListActivity
import com.app.motel.feature.handleContract.HandleContractActivity
import com.app.motel.feature.home.viewmodel.HomeViewModel
import com.app.motel.feature.room.RoomActivity
import com.app.motel.feature.rules.RulesActivity
import com.app.motel.feature.service.ServiceActivity
import com.app.motel.feature.tenant.TenantActivity
import javax.inject.Inject

class ManagementBoardingHouseFragment @Inject constructor() : AppBaseFragment<FragmentManagementBoardingHouseBinding>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val mViewModel : HomeViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(HomeViewModel::class.java)
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentManagementBoardingHouseBinding {
        return FragmentManagementBoardingHouseBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        listenStateViewModel()
    }

    private fun listenStateViewModel() {
        mViewModel.userController.state.currentUser.observe(viewLifecycleOwner){
            if (it != null) {
                if (it.data?.isAdmin == true) {
                    setupAdminUI()
                    return@observe
                }else{
                    setupUserUI()
                    return@observe
                }
            }
            views.lyAdmin.root.visibility = View.GONE
            views.lyUser.root.visibility = View.GONE
        }
    }

    private fun setupAdminUI() {
        views.lyAdmin.root.visibility = View.VISIBLE
        views.lyUser.root.visibility = View.GONE

        views.lyAdmin.lyCreateContract.img.setImageResource(R.drawable.contract)
        views.lyAdmin.lyCreateContract.title.text = "Tạo hợp đồng"
        views.lyAdmin.lyCreateContract.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), CreateContractActivity::class.java))
        }

        views.lyAdmin.lyService.img.setImageResource(R.drawable.bed)
        views.lyAdmin.lyService.title.text = "Dịch vụ"
        views.lyAdmin.lyService.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), ServiceActivity::class.java))
        }

        views.lyAdmin.lyHandleContract.img.setImageResource(R.drawable.handle_contract)
        views.lyAdmin.lyHandleContract.title.text = "Xử lý hợp đồng"
        views.lyAdmin.lyHandleContract.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), HandleContractActivity::class.java))
        }

        views.lyAdmin.lyCreateBill.img.setImageResource(R.drawable.create_bill)
        views.lyAdmin.lyCreateBill.title.text = "Tạo hóa đơn"
        views.lyAdmin.lyCreateBill.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), CreateBillActivity::class.java))
        }

        views.lyAdmin.lyListRoom.img.setImageResource(R.drawable.room)
        views.lyAdmin.lyListRoom.title.text = "Danh sách phòng thuê"
        views.lyAdmin.lyListRoom.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), RoomActivity::class.java))
        }

        views.lyAdmin.lyListTenant.img.setImageResource(R.drawable.tenant)
        views.lyAdmin.lyListTenant.title.text = "Danh sách khách thuê"
        views.lyAdmin.lyListTenant.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), TenantActivity::class.java))
        }

        views.lyAdmin.lyListBill.img.setImageResource(R.drawable.payment)
        views.lyAdmin.lyListBill.title.text = "Danh sách hóa đơn"
        views.lyAdmin.lyListBill.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), HandleBillActivity::class.java))
        }

        views.lyAdmin.lyListContract.img.setImageResource(R.drawable.list_contract)
        views.lyAdmin.lyListContract.title.text = "Danh sách hợp đồng"
        views.lyAdmin.lyListContract.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), ContractListActivity::class.java))
        }

        views.lyAdmin.lyRule.img.setImageResource(R.drawable.icon_rule)
        views.lyAdmin.lyRule.title.text = "Nội quy phòng trọ"
        views.lyAdmin.lyRule.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), RulesActivity::class.java))
        }
    }

    private fun setupUserUI() {
        views.lyAdmin.root.visibility = View.GONE
        views.lyUser.root.visibility = View.VISIBLE

        views.lyUser.lyContractDetail.img.setImageResource(R.drawable.contract)
        views.lyUser.lyContractDetail.title.text = "Chi tiết hợp đồng"
        views.lyUser.lyContractDetail.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), ContractListActivity::class.java))
        }

        views.lyUser.lyRoomRenting.img.setImageResource(R.drawable.bed)
        views.lyUser.lyRoomRenting.title.text = "Phòng đang thuê"
        views.lyUser.lyRoomRenting.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), RoomActivity::class.java).apply {
                putExtra(RoomActivity.ROOM_STATE_KEY, PhongEntity.Status.RENTED.value)
            })
        }

        views.lyUser.lySendComplaint.img.setImageResource(R.drawable.handle_contract)
        views.lyUser.lySendComplaint.title.text = "Gửi khiếu nại"
        views.lyUser.lySendComplaint.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), ComplaintActivity::class.java))
        }

        views.lyUser.lyBillPayment.img.setImageResource(R.drawable.create_bill)
        views.lyUser.lyBillPayment.title.text = "Hóa đơn chưa thanh toán"
        views.lyUser.lyBillPayment.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), HandleBillActivity::class.java).apply {
                putExtra(HandleBillActivity.BILL_STATE_KEY, HoaDonEntity.STATUS_UNPAID)
            })
        }

        views.lyUser.lyListRoomEmpty.img.setImageResource(R.drawable.room)
        views.lyUser.lyListRoomEmpty.title.text = "Danh sách phòng trống"
        views.lyUser.lyListRoomEmpty.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), RoomActivity::class.java).apply {
                putExtra(RoomActivity.ROOM_STATE_KEY, PhongEntity.Status.EMPTY.value)
            })
        }

        views.lyUser.lyBillPaid.img.setImageResource(R.drawable.payment)
        views.lyUser.lyBillPaid.title.text = "Hóa đơn đã thanh toán"
        views.lyUser.lyBillPaid.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), HandleBillActivity::class.java).apply {
                putExtra(HandleBillActivity.BILL_STATE_KEY, HoaDonEntity.STATUS_PAID)
            })
        }

        views.lyUser.lyRule.img.setImageResource(R.drawable.icon_rule)
        views.lyUser.lyRule.title.text = "Nội quy phòng trọ"
        views.lyUser.lyRule.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), RulesActivity::class.java))
        }
    }
}