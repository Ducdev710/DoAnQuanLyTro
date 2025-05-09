package com.app.motel.feature.news

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.app.motel.common.service.DateConverter
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.entity.ThongBaoEntity
import com.app.motel.data.model.Notification
import com.app.motel.databinding.ItemNewsBinding

class NewsAdapter(
    val listener: AppListener<Notification>
): AppBaseAdapter<Notification, ItemNewsBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemNewsBinding {
         return ItemNewsBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun bind(binding: ItemNewsBinding, item: Notification, position: Int) {
        binding.tvCreateDate.text = DateConverter.stringToDate(item.createdDate)?.let {
            DateConverter.dateToLocalString2(it)
        }
        binding.tvTitle.text = item.title
        binding.tvContent.text = item.content
        binding.tvFocus.text = "Đối tượng: ${item.room?.roomName ?: "Tất cả các phòng"}"
    }
}