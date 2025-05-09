package com.app.motel.feature.room

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.app.motel.AppApplication
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Room
import com.app.motel.databinding.FragmentRoomDetailBinding
import com.app.motel.feature.room.viewmodel.RoomViewModel
import com.app.motel.ui.adapter.ViewPagerAdapter
import com.app.motel.ui.custom.CustomTabBar
import com.google.gson.Gson
import javax.inject.Inject

class RoomDetailFragment @Inject constructor() : AppBaseFragment<FragmentRoomDetailBinding>() {

    val fragments = arrayListOf(
        RoomDetailInformationFragment(),
        RoomDetailTenantFragment(),
    )

    companion object{
        const val ITEM_KEY = "room_item"
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRoomDetailBinding {
        return FragmentRoomDetailBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val mViewModel : RoomViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(RoomViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        val item = Gson().fromJson(arguments?.getString(ITEM_KEY), Room::class.java)
        mViewModel.initRoomDetail(item)
        views.tabBar.setOnTabSelectedListener(object: CustomTabBar.OnTabSelectedListener{
            override fun onTabSelected(position: Int) {
                views.viewPager.currentItem = position
            }
        })

        val adapter = ViewPagerAdapter(
            fragments,
            childFragmentManager,
            lifecycle
        )

        views.viewPager.adapter = adapter
        views.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                views.tabBar.setTabSelected(position)
                views.viewPager.currentItem = position
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.clearStateCreate()
    }
}