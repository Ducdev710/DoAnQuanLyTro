package com.app.motel.feature.revenue

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.databinding.ActivityRevenueStatisticsBinding

class RevenueStatisticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRevenueStatisticsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as AppApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityRevenueStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        (binding.toolbar.context as AppCompatActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_new_24)
        binding.toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.white))
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        binding.toolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.white))
        binding.toolbar.overflowIcon?.setTint(ContextCompat.getColor(this, R.color.white))
        binding.toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleStyle)
        binding.toolbar.isTitleCentered = true
        supportActionBar?.title = "Thống kê các khoản thu"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}