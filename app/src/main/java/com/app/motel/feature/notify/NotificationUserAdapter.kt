package com.app.motel.feature.notify

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.app.motel.R
import com.app.motel.common.service.DateConverter
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.model.Notification
import com.app.motel.databinding.ItemNotificationBinding

class NotificationUserAdapter(
    val listener: AppBaseAdapter.AppListener<Notification>
): AppBaseAdapter<Notification, ItemNotificationBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemNotificationBinding {
        return ItemNotificationBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun bind(binding: ItemNotificationBinding, item: Notification, position: Int) {
        // Set notification content
        binding.tvTitle.text = item.title
        binding.tvContent.text = item.content
        binding.tvCreateDate.text = DateConverter.stringToDate(item.createdDate ?: "")?.let {
            DateConverter.dateToLocalString2(it)
        }

        // Apply visual distinction for unread notifications
        if (item.isRead != 1) {
            // Unread notification styling (isRead = 0 or null)
            binding.tvTitle.setTypeface(binding.tvTitle.typeface, Typeface.BOLD)
            binding.tvContent.setTypeface(binding.tvContent.typeface, Typeface.BOLD)
            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.background3))
        } else {
            // Read notification styling (isRead = 1)
            binding.tvTitle.setTypeface(binding.tvTitle.typeface, Typeface.NORMAL)
            binding.tvContent.setTypeface(binding.tvContent.typeface, Typeface.NORMAL)
            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.white))
        }

        binding.root.setOnClickListener {
            listener.onClickItem(item)
        }
    }
}