package com.app.motel.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.motel.core.AppBaseFragment

class ViewPagerAdapter(
    private var views: ArrayList<AppBaseFragment<out ViewBinding>>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return views.size
    }
    override fun createFragment(position: Int): Fragment {
        return views[position]
    }
}