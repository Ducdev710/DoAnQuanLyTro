package com.app.motel.feature.home

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import java.util.Calendar
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
        // First load all necessary data
        val boardingHouseId = viewModel.userController.state.currentBoardingHouseId
        viewModel.getBoardingById(boardingHouseId)
        viewModel.getBills()
        viewModel.getContracts()

        viewModel.liveData.boardingHouse.observe(viewLifecycleOwner){ boardingHouseResource ->
            if(boardingHouseResource.isSuccess()){
                val boardingHouse = boardingHouseResource.data
                views.tvNameBoardingHouse.text = boardingHouse?.name
                views.tvBoardingHouseTotalRoom.text = (boardingHouse?.rooms?.size ?: 0).toString()

                views.lyRoomEmpty.tvPosition.text = (boardingHouse?.getRoomEmpty?.size ?: 0).toString()
                views.lyRoomRenting.tvPosition.text = (boardingHouse?.getRoomRenting?.size ?: 0).toString()

                setupRoomStatusChart()
            }
        }

        viewModel.liveData.contracts.observe(viewLifecycleOwner){ contractsResource ->
            if(contractsResource.isSuccess()){
                // Get unique room IDs with near-end contracts
                val roomsWithNearEndContracts = contractsResource.data
                    ?.filter { it.isNearEnd }
                    ?.mapNotNull { it.roomId }
                    ?.filter { !it.isNullOrBlank() }
                    ?.toSet()
                    ?: emptySet()

                // Update UI with count
                views.lyRoomNearEnd.tvPosition.text = roomsWithNearEndContracts.size.toString()

                Log.d("Dashboard", "Near-end contracts: ${contractsResource.data?.count { it.isNearEnd } ?: 0}")
                Log.d("Dashboard", "Unique rooms with near-end contracts: ${roomsWithNearEndContracts.size}")

                setupRoomStatusChart()
            }
        }

        viewModel.liveData.bills.observe(viewLifecycleOwner){ billsResource ->
            if(billsResource.isSuccess()){
                // Get unpaid bills
                val unpaidBills = billsResource.data?.filter {
                    it.status == HoaDonEntity.STATUS_UNPAID
                } ?: emptyList()

                // Get unique room IDs with unpaid bills
                val uniqueUnpaidRooms = unpaidBills
                    .mapNotNull { it.roomId }
                    .filter { !it.isNullOrBlank() }
                    .toSet()

                // Update UI with count
                views.lyRoomNotPayBill.tvPosition.text = uniqueUnpaidRooms.size.toString()

                Log.d("Dashboard", "Unpaid bills: ${unpaidBills.size}")
                Log.d("Dashboard", "Unique rooms with unpaid bills: ${uniqueUnpaidRooms.size}")

                setupRoomStatusChart()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupRoomStatusChart() {
        val chart = views.chartRoomStatus

        // Configure chart
        chart.description.isEnabled = false
        chart.setUsePercentValues(true)
        chart.setDrawHoleEnabled(true)
        chart.setHoleColor(Color.WHITE)
        chart.setTransparentCircleRadius(61f)
        chart.setCenterText("Tình trạng phòng")
        chart.setCenterTextSize(14f)
        chart.animateY(1000)

        // Create legend
        val legend = chart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.textSize = 12f

        // Get data
        val totalRooms = viewModel.liveData.boardingHouse.value?.data?.rooms?.size ?: 0
        val emptyRooms = viewModel.liveData.boardingHouse.value?.data?.getRoomEmpty?.size ?: 0
        val rentedRooms = viewModel.liveData.boardingHouse.value?.data?.getRoomRenting?.size ?: 0

        // Calculate empty room percentage
        val emptyRoomPercentage = if (totalRooms > 0) (emptyRooms.toFloat() / totalRooms) * 100 else 0f

        // Create pie entries - using only base categories that don't overlap
        val entries = ArrayList<PieEntry>()
        if (emptyRooms > 0) entries.add(PieEntry(emptyRooms.toFloat(), "Phòng trống (${emptyRooms}/${totalRooms})"))
        if (rentedRooms > 0) entries.add(PieEntry(rentedRooms.toFloat(), "Đang thuê (${rentedRooms}/${totalRooms})"))

        if (entries.isEmpty()) return

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.rgb(176, 224, 230), // light blue
            Color.rgb(144, 238, 144)  // light green
        )
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        val data = PieData(dataSet)
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK)

        chart.data = data
        chart.invalidate()

        // Display recommendation based on empty room percentage
        views.tvRecommendation.visibility = View.VISIBLE
        when {
            emptyRoomPercentage >= 50 -> {
                views.tvRecommendation.text = "⚠️ Cảnh báo: Có ${emptyRooms}/${totalRooms} phòng trống (${String.format("%.1f", emptyRoomPercentage)}%). Nên xem xét giảm giá phòng hoặc cải thiện tiện nghi để thu hút người thuê."
                views.tvRecommendation.setTextColor(Color.parseColor("#FF5722")) // Deep orange
            }
            emptyRoomPercentage >= 30 -> {
                views.tvRecommendation.text = "⚠️ Lưu ý: Có ${emptyRooms}/${totalRooms} phòng trống (${String.format("%.1f", emptyRoomPercentage)}%). Cần đẩy mạnh quảng cáo hoặc cải thiện dịch vụ để tăng tỷ lệ lấp đầy."
                views.tvRecommendation.setTextColor(Color.parseColor("#FF9800")) // Orange
            }
            emptyRoomPercentage >= 15 -> {
                views.tvRecommendation.text = "ℹ️ Gợi ý: Có ${emptyRooms}/${totalRooms} phòng trống (${String.format("%.1f", emptyRoomPercentage)}%). Đây là tỷ lệ trống hợp lý để duy trì khả năng cho thuê."
                views.tvRecommendation.setTextColor(Color.parseColor("#2196F3")) // Blue
            }
            emptyRoomPercentage > 0 -> {
                views.tvRecommendation.text = "✅ Tốt: Chỉ còn ${emptyRooms}/${totalRooms} phòng trống (${String.format("%.1f", emptyRoomPercentage)}%). Có thể xem xét tăng giá phòng ở chu kỳ tiếp theo."
                views.tvRecommendation.setTextColor(Color.parseColor("#4CAF50")) // Green
            }
            else -> {
                views.tvRecommendation.text = "🌟 Tuyệt vời: Đã cho thuê 100% số phòng. Có thể xem xét mở rộng khu trọ hoặc tăng giá phòng ở chu kỳ tiếp theo."
                views.tvRecommendation.setTextColor(Color.parseColor("#4CAF50")) // Green
            }
        }
    }

    /*private fun setupRevenueSourcesChart() {
        val chart = views.chartRevenueDistribution

        // Configure chart
        chart.description.isEnabled = false
        chart.setUsePercentValues(true)
        chart.setDrawHoleEnabled(true)
        chart.setHoleColor(Color.WHITE)
        chart.setTransparentCircleRadius(61f)
        chart.setCenterText("Nguồn thu")
        chart.setCenterTextSize(14f)
        chart.animateY(1000)

        // Create legend
        val legend = chart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.textSize = 12f

        // Get revenue data from your viewModel (you may need to add this data to your model)
        // For now using dummy data - replace with actual data
        val roomTotal = 70f
        val serviceTotal = 15f
        val electricTotal = 10f
        val waterTotal = 5f

        // Create pie entries
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(roomTotal, "Tiền phòng"))
        entries.add(PieEntry(serviceTotal, "Dịch vụ"))
        entries.add(PieEntry(electricTotal, "Tiền điện"))
        entries.add(PieEntry(waterTotal, "Tiền nước"))

        if (entries.isEmpty()) return

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.rgb(135, 206, 235), // sky blue
            Color.rgb(152, 251, 152), // pale green
            Color.rgb(255, 160, 122), // light salmon
            Color.rgb(221, 160, 221)  // plum
        )
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        val data = PieData(dataSet)
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK)
        data.setValueFormatter(PercentFormatter(chart))

        chart.data = data
        chart.invalidate()
    }*/
    /*private fun setupMonthlyTrendChart() {
        try {
            // Get a reference to the chart view
            val chart = views.chartMonthlyTrend

            // Set a default "no data" message
            chart.setNoDataText("Đang tải dữ liệu...")
            chart.setNoDataTextColor(Color.BLACK)

            // Reset chart to clear any previous configurations
            chart.clear()

            // Basic configuration
            chart.description.isEnabled = false
            chart.setTouchEnabled(true)
            chart.setDrawGridBackground(false)
            chart.animateX(1500)
            chart.axisRight.isEnabled = false
            chart.legend.textSize = 12f
            chart.setExtraOffsets(10f, 10f, 10f, 10f)

            // X-axis styling
            val xAxis = chart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.textSize = 12f
            xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                private val months = arrayOf("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12")
                override fun getFormattedValue(value: Float): String {
                    val index = Math.floorMod(value.toInt(), 12)
                    return months[index]
                }
            }

            // Y-axis styling
            val yAxis = chart.axisLeft
            yAxis.setDrawGridLines(true)
            yAxis.textSize = 12f
            yAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%,.0f", value)
                }
            }

            // Start with simple guaranteed data to ensure chart displays
            val entries = ArrayList<Entry>()
            entries.add(Entry(0f, 5000000f))

            // Get bills from viewModel after ensuring we have minimum data
            val bills = viewModel.liveData.bills.value?.data ?: listOf()

            // Only add real data if we have bills
            if (bills.isNotEmpty()) {
                entries.clear() // Clear the guaranteed data

                val currentCalendar = Calendar.getInstance()
                val startMonth = currentCalendar.get(Calendar.MONTH)
                val currentYear = currentCalendar.get(Calendar.YEAR)

                // Debug info
                android.util.Log.d("ChartDebug", "Start month: $startMonth, year: $currentYear, bills count: ${bills.size}")

                for (i in 0..5) {
                    val monthToShow = (startMonth + i) % 12
                    val yearOffset = (startMonth + i) / 12
                    val year = currentYear + yearOffset

                    // Get paid bills for this month/year
                    val paidBills = bills.filter { bill ->
                        try {
                            val billMonth = bill.month ?: return@filter false
                            val billYear = bill.year ?: return@filter false

                            val isMatch = billMonth - 1 == monthToShow && billYear == year && bill.status == HoaDonEntity.STATUS_PAID
                            if (isMatch) {
                                Log.d("ChartDebug", "Matching bill found for month $monthToShow")
                            }
                            isMatch
                        } catch (e: Exception) {
                            Log.e("ChartDebug", "Error filtering bill: ${e.message}")
                            false
                        }
                    }

                    // Sum up revenue
                    val monthlyRevenue = paidBills.sumOf { bill ->
                        bill.totalAmount?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                    }

                    Log.d("ChartDebug", "Month $monthToShow revenue: $monthlyRevenue")

                    // Always add entry even if revenue is 0
                    entries.add(Entry(i.toFloat(), monthlyRevenue.toFloat()))
                }

                Log.d("ChartDebug", "Created ${entries.size} entries: ${entries.map { it.y }}")
            }

            // Ensure we have data
            if (entries.isEmpty()) {
                Log.d("ChartDebug", "No entries found, adding default entry")
                entries.add(Entry(0f, 5000000f))
            }

            // Create and configure the dataset
            val dataSet = LineDataSet(entries, "Doanh thu (VND)")
            dataSet.color = Color.rgb(65, 105, 225)
            dataSet.setCircleColor(Color.rgb(65, 105, 225))
            dataSet.lineWidth = 2.5f
            dataSet.circleRadius = 5f
            dataSet.setDrawFilled(true)
            dataSet.fillAlpha = 50
            dataSet.fillColor = Color.rgb(65, 105, 225)
            dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            dataSet.setDrawValues(true)
            dataSet.valueTextSize = 10f
            dataSet.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%,.0f", value)
                }
            }

            val lineData = LineData(dataSet)
            chart.data = lineData

            // Final invalidate to refresh the chart
            chart.invalidate()

            Log.d("ChartDebug", "Chart setup complete")
        } catch (e: Exception) {
            Log.e("ChartDebug", "Error setting up chart: ${e.message}", e)
        }
    }*/
}