package com.app.motel.feature.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.utils.startActivityWithSlide
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.entity.HoaDonEntity
import com.app.motel.data.entity.PhongEntity
import com.app.motel.data.model.Bill
import com.app.motel.data.model.Contract
import com.app.motel.databinding.FragmentGeneralBoardingHouseBinding
import com.app.motel.feature.handleBill.HandleBillActivity
import com.app.motel.feature.handleContract.HandleContractActivity
import com.app.motel.feature.home.viewmodel.HomeViewModel
import com.app.motel.feature.revenue.RevenueStatisticsActivity
import com.app.motel.feature.room.RoomActivity
import javax.inject.Inject

class GeneralBoardingHouseFragment @Inject constructor() : AppBaseFragment<FragmentGeneralBoardingHouseBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentGeneralBoardingHouseBinding {
        return FragmentGeneralBoardingHouseBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : HomeViewModel by lazy{
        ViewModelProvider(requireActivity(), viewModelFactory).get(HomeViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        setup()
        handleObserverData()
    }
    private fun setup() {
        views.lyRoomEmpty.img.setImageResource(R.drawable.icon_rooms_available)
        views.lyRoomEmpty.title.text = "Số phòng đang trống"
        views.lyRoomEmpty.tvPosition.text = "2"
        views.lyRoomEmpty.tvPosition.isVisible = true
        views.lyRoomEmpty.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), RoomActivity::class.java).apply {
                putExtra(RoomActivity.ROOM_STATE_KEY, PhongEntity.Status.EMPTY.value)
            })
        }

        views.lyRoomRenting.img.setImageResource(R.drawable.icon_key_hotel)
        views.lyRoomRenting.title.text = "Số phòng đang thuê"
        views.lyRoomRenting.tvPosition.text = "2"
        views.lyRoomRenting.tvPosition.isVisible = true
        views.lyRoomRenting.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), RoomActivity::class.java).apply {
                putExtra(RoomActivity.ROOM_STATE_KEY, PhongEntity.Status.RENTED.value)
            })
        }

        views.lyRoomNearEnd.img.setImageResource(R.drawable.icon_timmer)
        views.lyRoomNearEnd.title.text = "Số phòng sắp hết hợp đồng"
        views.lyRoomNearEnd.tvPosition.text = "2"
        views.lyRoomNearEnd.tvPosition.isVisible = true
        views.lyRoomNearEnd.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), HandleContractActivity::class.java).apply {
                putExtra(HandleContractActivity.CONTRACT_STATE_KEY, Contract.State.NEAR_END.value)
            })
        }

        views.lyRoomNotPayBill.img.setImageResource(R.drawable.icon_loan)
        views.lyRoomNotPayBill.title.text = "Số phòng chưa đóng tiền"
        views.lyRoomNotPayBill.tvPosition.text = "2"
        views.lyRoomNotPayBill.tvPosition.isVisible = true
        views.lyRoomNotPayBill.root.setOnClickListener{
            requireActivity().startActivityWithSlide(Intent(requireActivity(), HandleBillActivity::class.java).apply {
                putExtra(HandleBillActivity.BILL_STATE_KEY, HoaDonEntity.STATUS_UNPAID)
            })
        }
        views.btnRevenue.setOnClickListener {
            requireActivity().startActivityWithSlide(Intent(requireActivity(), RevenueStatisticsActivity::class.java))
        }
    }

    private fun handleObserverData() {
        viewModel.liveData.boardingHouse.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                views.tvNameBoardingHouse.text = viewModel.liveData.boardingHouse.value?.data?.name
                views.tvBoardingHouseTotalRoom.text = (viewModel.liveData.boardingHouse.value?.data?.rooms?.size ?: 0).toString()

                views.lyRoomEmpty.tvPosition.text = viewModel.liveData.boardingHouse.value?.data?.getRoomEmpty?.size.toString()
                views.lyRoomRenting.tvPosition.text = viewModel.liveData.boardingHouse.value?.data?.getRoomRenting?.size.toString()
            }
        }
        viewModel.liveData.contracts.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                views.lyRoomNearEnd.tvPosition.text = (viewModel.liveData.contracts.value?.data?.filter { it.isNearEnd }?.size ?: 0).toString()
            }
        }
        viewModel.liveData.bills.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                val listBillNotPayed = viewModel.liveData.bills.value?.data?.filter { it.status == HoaDonEntity.STATUS_UNPAID } ?: arrayListOf()
                val roomNotPayed: Map<String, Bill> = listBillNotPayed.associateBy { it.roomId ?: "" }
                views.lyRoomNotPayBill.tvPosition.text = roomNotPayed.values.size.toString()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getContracts()
        viewModel.getBills()
    }
}