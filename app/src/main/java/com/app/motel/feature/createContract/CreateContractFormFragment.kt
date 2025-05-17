package com.app.motel.feature.createContract

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.service.DateConverter
import com.app.motel.common.utils.showToast
import com.app.motel.common.utils.toStringMoney
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Room
import com.app.motel.data.model.Status
import com.app.motel.data.model.Tenant
import com.app.motel.databinding.FragmentCreateContractFormBinding
import com.app.motel.feature.createContract.viewmodel.CreateContractViewModel
import com.google.gson.Gson
import java.util.Calendar
import javax.inject.Inject

class CreateContractFormFragment @Inject constructor() : AppBaseFragment<FragmentCreateContractFormBinding>() {
    companion object{
        const val ITEM_KEY = "room_item"
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCreateContractFormBinding {
        return FragmentCreateContractFormBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val mViewModel : CreateContractViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(CreateContractViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        init()
        listenerViewModelState()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun init() {
        val item = Gson().fromJson(arguments?.getString(ITEM_KEY), Room::class.java)

        views.txtName.setText(item?.roomName ?: "")

        // Set contract creation date to current date
        val contractDateStr = DateConverter.getCurrentLocalDateTime()
        views.txtContractDate.setText(contractDateStr)

        // Set default start date to tomorrow (one day after contract creation)
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val startDateStr = DateConverter.dateToLocalString(tomorrow.time)

        // Set default end date to 1 year from start date
        val endDateStr = DateConverter.dateToLocalString(Calendar.getInstance().apply {
            time = tomorrow.time
            add(Calendar.YEAR, 1)
        }.time)

        // Store actual dates for backend
        views.txtStartDate.setText(startDateStr)
        views.txtEndDate.setText(endDateStr)

        // Calculate and display initial duration
        updateDurationText(startDateStr, endDateStr)

        views.txtDeposit.setText(item?.rentalPrice.toStringMoney())

        // Set up date picker functionality for start date
        views.txtStartDate.setOnClickListener {
            showStartDatePickerDialog { selectedDate ->
                views.txtStartDate.setText(selectedDate)
                // Update duration when start date changes
                updateDurationText(selectedDate, views.txtEndDate.text.toString())
            }
        }

        // Set up date picker functionality for end date
        views.txtEndDate.setOnClickListener {
            showEndDatePickerDialog(views.txtStartDate.text.toString()) { selectedDate ->
                views.txtEndDate.setText(selectedDate)
                // Update duration when end date changes
                updateDurationText(views.txtStartDate.text.toString(), selectedDate)
            }
        }

        mViewModel.getTenantNotRented()

        views.btnSave.setOnClickListener {
            val currentTenantPosition = views.spinnerCustomer.selectedItemPosition
            val currentTenant: Tenant? = if(currentTenantPosition >= 0)
                mViewModel.liveData.tenantNotRented.value?.data?.get(currentTenantPosition)
            else null

            mViewModel.createContact(
                item.id,
                currentTenant?.id,
                views.txtName.text.toString(),
                views.txtContractDate.text.toString(), // Contract creation date
                views.txtStartDate.text.toString(),    // Start date (move-in)
                views.txtEndDate.text.toString(),
                views.txtDeposit.text.toString().toStringMoney(),
                views.txtNote.text.toString(),
            )
        }
    }

    /**
     * Update the contract duration text based on start and end dates
     */
    private fun updateDurationText(startDateStr: String, endDateStr: String) {
        val durationMonths = calculateDurationInMonths(startDateStr, endDateStr)
        views.txtCreateDate.setText("$durationMonths tháng")
    }

    /**
     * Show date picker dialog for start date
     * Minimum date is tomorrow
     */
    private fun showStartDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentStartDate = DateConverter.stringToDate(views.txtStartDate.text.toString())
        if (currentStartDate != null) {
            calendar.time = currentStartDate
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val selectedDate = DateConverter.dateToLocalString(calendar.time)
                onDateSelected(selectedDate)
            },
            year, month, day
        )

        // Set minimum date to tomorrow
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_MONTH, 1)
        datePickerDialog.datePicker.minDate = tomorrow.timeInMillis

        datePickerDialog.show()
    }

    /**
     * Show date picker dialog for end date
     * Minimum date is day after start date
     */
    private fun showEndDatePickerDialog(startDateStr: String, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentEndDate = DateConverter.stringToDate(views.txtEndDate.text.toString())
        if (currentEndDate != null) {
            calendar.time = currentEndDate
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val selectedDate = DateConverter.dateToLocalString(calendar.time)
                onDateSelected(selectedDate)
            },
            year, month, day
        )

        // Set minimum date to day after start date
        val minDate = DateConverter.stringToDate(startDateStr)
        if (minDate != null) {
            val minCalendar = Calendar.getInstance().apply {
                time = minDate
                add(Calendar.DAY_OF_MONTH, 1)
            }
            datePickerDialog.datePicker.minDate = minCalendar.timeInMillis
        }

        datePickerDialog.show()
    }

    /**
     * Calculate duration between two dates in months
     */
    private fun calculateDurationInMonths(startDateStr: String, endDateStr: String): Int {
        try {
            val startDate = DateConverter.stringToDate(startDateStr)
            val endDate = DateConverter.stringToDate(endDateStr)

            if (startDate != null && endDate != null) {
                val startCalendar = Calendar.getInstance().apply { time = startDate }
                val endCalendar = Calendar.getInstance().apply { time = endDate }

                val yearDiff = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)
                val monthDiff = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH)

                return yearDiff * 12 + monthDiff
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 12 // Default to 12 months if calculation fails
    }

    private fun listenerViewModelState() {
        mViewModel.liveData.tenantNotRented.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                val tenants = mViewModel.liveData.tenantNotRented.value?.data ?: arrayListOf()
                val adapter = ArrayAdapter(
                    requireContext(), // Context
                    R.layout.item_spinner_text, // Layout
                    tenants.map { tenant: Tenant ->  tenant.fullName}
                )

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                views.spinnerCustomer.adapter = adapter

                handleTenantSelected(tenants)
            }
        }

        mViewModel.liveData.createContract.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    requireActivity().showToast("Tạo hợp đồng thành công")
                    if(mViewModel.liveData.currentTenantId != null){
                        requireActivity().setResult(Activity.RESULT_OK, Intent())
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                Status.ERROR -> {
                    activity?.showToast(it.message ?: "Có lỗi xảy ra")
                }
                else -> {}
            }
        }
    }

    private fun handleTenantSelected(tenants: List<Tenant>) {
        if(mViewModel.liveData.currentTenantId != null){
            (tenants.indexOfFirst { tenant: Tenant ->  tenant.id == mViewModel.liveData.currentTenantId  }).let { position ->
                if(position != -1){
                    views.spinnerCustomer.setSelection(position)
                }else{
                    mViewModel.liveData.currentTenantId = null
                    requireActivity().showToast("Không tìm thấy người muốn thuê")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.clearForm()
    }
}