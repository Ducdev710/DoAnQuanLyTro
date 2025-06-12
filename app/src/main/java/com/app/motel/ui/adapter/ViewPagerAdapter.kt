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

    //Trả về số lượng fragment trong adapter
    //Được gọi bởi ViewPager2 để xác định có bao nhiêu trang
    override fun getItemCount(): Int {
        return views.size
    }

    //Trả về fragment tại vị trí được chỉ định
    //Được gọi bởi ViewPager2 khi cần tạo hoặc lấy fragment cho một trang cụ thể
    override fun createFragment(position: Int): Fragment {
        return views[position]
    }
}