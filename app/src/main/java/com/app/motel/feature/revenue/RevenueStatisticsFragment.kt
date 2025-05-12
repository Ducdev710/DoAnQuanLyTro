package com.app.motel.feature.revenue

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.motel.AppApplication
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseDialog
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Bill
import com.app.motel.databinding.DialogDatePickerBinding
import com.app.motel.databinding.FragmentRevenueStatisticsBinding
import com.app.motel.feature.handleBill.BillAdapter
import com.app.motel.feature.handleBill.HandleDetailBillBottomSheet
import com.app.motel.feature.revenue.viewmodel.RevenueViewModel
import java.util.Calendar
import javax.inject.Inject

class RevenueStatisticsFragment @Inject constructor() : AppBaseFragment<FragmentRevenueStatisticsBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRevenueStatisticsBinding {
        return FragmentRevenueStatisticsBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: RevenueViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory)[RevenueViewModel::class.java]
    }

    private lateinit var billAdapter: BillAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        init()
        listenStateViewModel()
    }

    private fun setupToolbar() {
        val toolbarTitle = arguments?.getString("TOOLBAR_TITLE") ?: "Thống kê các khoản thu"
        (activity as? AppCompatActivity)?.supportActionBar?.title = toolbarTitle
    }

    private fun init() {
        // Setup adapter
        billAdapter = BillAdapter(object : AppBaseAdapter.AppListener<Bill>() {
            override fun onClickItem(item: Bill, action: AppBaseAdapter.ItemAction) {
                HandleDetailBillBottomSheet(item).show(parentFragmentManager, HandleDetailBillBottomSheet::class.java.simpleName)
            }
        })
        views.rcv.adapter = billAdapter
        views.rcv.layoutManager = LinearLayoutManager(requireContext())

        // Load initial data for current month
        viewModel.getPaidBillsByMonth(viewModel.currentMonth)

        // Set up navigation buttons
        views.btnPreviousMonth.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH, viewModel.currentMonth - 2) // -2 because months are 0-based and we want previous month
            calendar.set(Calendar.YEAR, viewModel.currentYear)
            if (calendar.get(Calendar.MONTH) < 0) {
                calendar.set(Calendar.MONTH, 11)
                calendar.set(Calendar.YEAR, viewModel.currentYear - 1)
            }
            viewModel.currentMonth = calendar.get(Calendar.MONTH) + 1
            viewModel.currentYear = calendar.get(Calendar.YEAR)
            updateMonth()
        }

        views.btnForwardMonth.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH, viewModel.currentMonth) // Current month is already 1-based
            calendar.set(Calendar.YEAR, viewModel.currentYear)
            if (calendar.get(Calendar.MONTH) > 11) {
                calendar.set(Calendar.MONTH, 0)
                calendar.set(Calendar.YEAR, viewModel.currentYear + 1)
            }
            viewModel.currentMonth = calendar.get(Calendar.MONTH) + 1
            viewModel.currentYear = calendar.get(Calendar.YEAR)
            updateMonth()
        }

        views.tvMonth.setOnClickListener {
            showDialogDatePicker()
        }

        // Initial UI update
        updateMonth()
    }

    private fun updateMonth() {
        views.tvMonth.text = "Tháng ${viewModel.currentMonth}/${viewModel.currentYear}"
        viewModel.getPaidBillsByMonth(viewModel.currentMonth)
    }

    @SuppressLint("SetTextI18n")
    private fun listenStateViewModel() {
        viewModel.liveData.bills.observe(viewLifecycleOwner) { resource ->
            if (resource.isSuccess()) {
                val bills = resource.data ?: emptyList()
                billAdapter.updateData(bills)

                // Calculate total revenue
                var totalRevenue = 0.0
                bills.forEach { bill ->
                    val amount = bill.totalAmount?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                    totalRevenue += amount
                }

                // Update UI with total revenue
                views.tvTotalRevenueValue.text = String.format("%,.0f", totalRevenue)

                // Show/hide empty state
                views.tvEmpty.isVisible = bills.isEmpty()
                views.rcv.isVisible = bills.isNotEmpty()
            }
        }
    }

    private fun showDialogDatePicker() {
        val dialog = AppBaseDialog.Builder(requireContext(), DialogDatePickerBinding.inflate(layoutInflater))
            .build()
        dialog.show()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, viewModel.currentYear)
        calendar.set(Calendar.MONTH, viewModel.currentMonth - 1) // Calendar months are 0-based

        dialog.binding.datePickerDob.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, _ ->
            viewModel.currentMonth = monthOfYear + 1
            viewModel.currentYear = year
            updateMonth()
        }
    }
}