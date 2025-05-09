package com.app.motel.feature.handleBill

import android.annotation.SuppressLint
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseDialog
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.entity.HoaDonEntity
import com.app.motel.data.model.Bill
import com.app.motel.data.model.Contract
import com.app.motel.databinding.DialogDatePickerBinding
import com.app.motel.databinding.FragmentHandleBillListBinding
import com.app.motel.feature.handleBill.viewmodel.HandleBillViewModel
import com.app.motel.ui.custom.CustomTabBar
import javax.inject.Inject

class HandleBillListFragment : AppBaseFragment<FragmentHandleBillListBinding>() {

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHandleBillListBinding {
        return FragmentHandleBillListBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : HandleBillViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(HandleBillViewModel::class.java)
    }

    lateinit var adapter: BillAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        viewModel.liveData

        init()
        listenStateViewModel()
    }
    private fun init() {
        viewModel.getBills()

        adapter = BillAdapter(object: AppBaseAdapter.AppListener<Bill>(){
            override fun onClickItem(item: Bill, action: AppBaseAdapter.ItemAction) {
                HandleDetailBillBottomSheet(item).show(parentFragmentManager, HandleDetailBillBottomSheet::class.java.simpleName)
            }
        })
        views.rcv.adapter = adapter
        views.tabBar.setOnTabSelectedListener(object: CustomTabBar.OnTabSelectedListener{
            override fun onTabSelected(position: Int) {
                when(position){
                    0 -> viewModel.liveData.filterState.postValue(HoaDonEntity.STATUS_PAID)
                    1 -> viewModel.liveData.filterState.postValue(HoaDonEntity.STATUS_UNPAID)
                }
            }
        })
        views.tabBar.post {
            views.tabBar.setTabSelected(when(viewModel.liveData.filterState.value){
                HoaDonEntity.STATUS_PAID -> 0
                HoaDonEntity.STATUS_UNPAID -> 1
                else -> 0
            })
        }
        views.btnPreviousMonth.setOnClickListener {
            val calendar = viewModel.liveData.currentDate.value!!
            calendar.add(Calendar.MONTH, -1)
            viewModel.liveData.currentDate.postValue(calendar)
        }
        views.btnForwardMonth.setOnClickListener {
            val calendar = viewModel.liveData.currentDate.value!!
            calendar.add(Calendar.MONTH, 1)
            viewModel.liveData.currentDate.postValue(calendar)
        }

        views.tvMonth.setOnClickListener{
            showDialogDatePicker()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun listenStateViewModel() {
        viewModel.userController.state.currentUser.observe(viewLifecycleOwner){
            (it.data?.isAdmin == true).apply{
                views.tabBar.isVisible = this
                views.lyFilter.isVisible = this
            }
        }
        viewModel.liveData.bills.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                val tenants = viewModel.liveData.getListBillByFilter(viewModel.userController.state.isAdmin)
                adapter.updateData(tenants)

                views.tvEmpty.isVisible = tenants.isEmpty()
            }
        }
        viewModel.liveData.filterState.observe(viewLifecycleOwner){
            val tenants = viewModel.liveData.getListBillByFilter(viewModel.userController.state.isAdmin)

            adapter.updateData(tenants)
            views.tvEmpty.isVisible = tenants.isEmpty()
        }
        viewModel.liveData.currentDate.observe(viewLifecycleOwner){
            val tenants = viewModel.liveData.getListBillByFilter(viewModel.userController.state.isAdmin)
            adapter.updateData(tenants)

            views.tvEmpty.isVisible = tenants.isEmpty()
            views.tvMonth.text = "${it.get(Calendar.MONTH) + 1}/${it.get(Calendar.YEAR)}"
        }
    }

    private fun showDialogDatePicker(){
        val dialog = AppBaseDialog.Builder(requireContext(), DialogDatePickerBinding.inflate(layoutInflater))
            .build()
        dialog.show()

        val calendar = viewModel.liveData.currentDate.value!!
        dialog.binding.datePickerDob.init(calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(
            java.util.Calendar.DAY_OF_MONTH)
        ) { view, year, monthOfYear, dayOfMonth ->
            calendar.set(year, monthOfYear, dayOfMonth)
            viewModel.liveData.currentDate.postValue(calendar)
        }
    }
}