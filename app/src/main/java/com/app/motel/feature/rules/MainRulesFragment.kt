package com.app.motel.feature.rules

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
import com.app.motel.databinding.FragmentMainRulesBinding
import com.app.motel.feature.rules.viewmodel.RulesViewModel
import com.app.motel.ui.adapter.ViewPagerAdapter
import com.app.motel.ui.custom.CustomTabBar
import javax.inject.Inject

class MainRulesFragment @Inject constructor() : AppBaseFragment<FragmentMainRulesBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMainRulesBinding {
        return FragmentMainRulesBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : RulesViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(RulesViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        super.onViewCreated(view, savedInstanceState)

        listenStateViewmodel()
    }

    private fun initUI(enable: Boolean) {
        val fragments: ArrayList<AppBaseFragment<out ViewBinding>>  = if(enable) arrayListOf(
            RulesContentFragment(),
            RuleFormFragment(),
        ) else arrayListOf(
            RulesContentFragment(),
        )

        val adapter= ViewPagerAdapter(
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

        views.tabBar.isVisible = enable
        views.tabBar.setOnTabSelectedListener(object: CustomTabBar.OnTabSelectedListener{
            override fun onTabSelected(position: Int) {
                views.viewPager.currentItem = position
            }
        })
    }

    private fun listenStateViewmodel(){
        viewModel.userController.state.currentUser.observe(viewLifecycleOwner){
            initUI(it.data?.isAdmin == true)
        }
    }


}