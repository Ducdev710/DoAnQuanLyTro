package com.app.motel.feature.createBill

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.common.service.DateConverter
import com.app.motel.common.utils.popFragmentWithSlide
import com.app.motel.common.utils.showToast
import com.app.motel.common.utils.toMoney
import com.app.motel.common.utils.toStringMoney
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.entity.DichVuEntity
import com.app.motel.data.model.Room
import com.app.motel.data.model.Status
import com.app.motel.databinding.FragmentCreateBillFormBinding
import com.app.motel.feature.createBill.viewmodel.CreateBillViewModel
import com.google.gson.Gson
import javax.inject.Inject

class CreateBillFormFragment @Inject constructor() : AppBaseFragment<FragmentCreateBillFormBinding>() {
    companion object{
        const val ITEM_KEY = "room_item"
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCreateBillFormBinding {
        return FragmentCreateBillFormBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val mViewModel : CreateBillViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(CreateBillViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        init()
        listenerViewModelState()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun init() {
        val item: Room? = Gson().fromJson(arguments?.getString(ITEM_KEY), Room::class.java)
        mViewModel.initForm(item)

        views.btnSave.setOnClickListener{
            mViewModel.createBill(
                views.txtCreateDate.text.toString(),
                views.txtElectricityOld.text.toString().toIntOrNull(),
                views.txtElectricityNew.text.toString().toIntOrNull(),
                views.txtWaterOld.text.toString().toIntOrNull(),
                views.txtWaterNew.text.toString().toIntOrNull(),
                views.txtPriceService.text.toString().toMoney(),
                views.txtDiscount.text.toString().toMoney(),
                null
            )
        }
    }

    private fun listenerViewModelState() {
        mViewModel.liveData.currentRoom.observe(viewLifecycleOwner){ currentRoom ->
            if(currentRoom.isSuccess() && currentRoom.data != null){
                views.tvName.text = currentRoom.data.roomName
                views.txtRent.setText(currentRoom.data.rentalPrice.toStringMoney())
                views.txtCreateDate.setText(DateConverter.getCurrentLocalDateTime())
            }else if(currentRoom.isSuccess() && currentRoom.data == null){
                requireActivity().showToast(currentRoom.message ?: "Không tìm thấy phòng thuê")
                popFragmentWithSlide()
            }
        }
        mViewModel.liveData.currentServiceRoom.observe(viewLifecycleOwner){
            if(it.isSuccess() && it.data != null){
                val total = it.data.fold(0) { value, service ->
                    when (service.typePay) {
                        DichVuEntity.TypePay.TO_ROOM.typeName -> value + service.price.toMoney()
                        DichVuEntity.TypePay.TO_PERSON.typeName -> {
                            val totalPerson = mViewModel.liveData.currentRoom.value?.data?.tenants?.size ?: 0
                            value + service.price.toMoney() * totalPerson
                        }
                        else -> value
                    }
                }
                views.txtPriceService.setText(total.toString().toStringMoney())
            }
        }
        mViewModel.liveData.previousBill.observe(viewLifecycleOwner){
            Log.d("previousBill", "${it.data} ${it.status} ${it.message}")
            when(it.status){
                Status.SUCCESS -> {
                    views.txtElectricityOld.setText(it.data?.electricityIndex?.toString() ?: 0.toString())
                    views.txtWaterOld.setText(it.data?.waterIndex?.toString() ?: 0.toString())
                }
                Status.ERROR -> {
                    requireActivity().showToast(it.message ?: "Có lỗi xảy ra")
                }
                else -> {}
            }
        }
        mViewModel.liveData.createBill.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    requireActivity().showToast("Tao hợp đồng thành công")
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                Status.ERROR -> {
                    requireActivity().showToast(it.message ?: "Có lỗi xảy ra")
                }
                else -> {}
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.clearStateCreate()
    }
}