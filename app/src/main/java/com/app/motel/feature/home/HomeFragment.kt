package com.app.motel.feature.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.app.motel.AppApplication
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.entity.HoaDonEntity
import com.app.motel.databinding.FragmentHomeBinding
import com.app.motel.feature.home.viewmodel.HomeViewModel
import com.app.motel.ui.adapter.ViewPagerAdapter
import com.app.motel.ui.custom.CustomTabBar
import javax.inject.Inject

class HomeFragment @Inject constructor() : AppBaseFragment<FragmentHomeBinding>() {


    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val mViewModel : HomeViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(HomeViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        listenStateViewModel()

        super.onViewCreated(view, savedInstanceState)
    }

    private fun listenStateViewModel() {
        mViewModel.userController.state.currentUser.observe(viewLifecycleOwner){
            setup(it.data?.isAdmin == true)
        }
    }

    private fun setup(isAdmin: Boolean) {
        views.tabBar.isVisible = isAdmin
        if(isAdmin){
            views.tabBar.post {
                views.tabBar.setTabSelected(mViewModel.liveData.currentTab.value ?: 0)
            }
            views.tabBar.setOnTabSelectedListener(object: CustomTabBar.OnTabSelectedListener{
                override fun onTabSelected(position: Int) {
                    mViewModel.liveData.currentTab.postValue(position)
                    views.viewPager.currentItem = position
                }
            })
        }

        val fragments: ArrayList<AppBaseFragment<out ViewBinding>> = if(isAdmin) arrayListOf(
            ManagementBoardingHouseFragment(),
            GeneralBoardingHouseFragment(),
        ) else arrayListOf(
            ManagementBoardingHouseFragment(),
        )

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

}