package com.app.motel.feature.notify

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.motel.R
import com.app.motel.common.service.DateConverter
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.entity.KhieuNaiEntity
import com.app.motel.data.model.Complaint
import com.app.motel.data.model.Notification
import com.app.motel.databinding.ItemComplaintBinding
import com.app.motel.databinding.ItemNotificationBinding

class NotificationUserAdapter(
    val listener: AppBaseAdapter.AppListener<Notification>
): AppBaseAdapter<Notification, ItemNotificationBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemNotificationBinding {
        return ItemNotificationBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun bind(binding: ItemNotificationBinding, item: Notification, position: Int) {

        binding.tvTitle.text = item.title
        binding.tvContent.text = item.content
        binding.tvCreateDate.text = DateConverter.stringToDate(item.createdDate ?: "")?.let {
           DateConverter.dateToLocalString2(it)
        }

        binding.root.setOnClickListener {
            listener.onClickItem(item)
        }

    }
}