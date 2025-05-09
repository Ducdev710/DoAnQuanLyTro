package com.app.motel.feature.complaint

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.app.motel.R
import com.app.motel.common.service.DateConverter
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.entity.KhieuNaiEntity
import com.app.motel.data.model.Complaint
import com.app.motel.databinding.ItemComplaintBinding

class ComplaintAdapter constructor(
    val listener: AppBaseAdapter.AppListener<Complaint>? = null
): AppBaseAdapter<Complaint, ItemComplaintBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemComplaintBinding {
        return ItemComplaintBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun bind(binding: ItemComplaintBinding, item: Complaint, position: Int) {
        binding.tvRoomName.text = "Phòng: ${item.room?.roomName ?: ""}"
        binding.tvStatus.text = item.status

        binding.tvTitle.text = item.title
        binding.tvContent.text = item.content
        binding.tvCreateDate.text = DateConverter.stringToDate(item.createdDate ?: "")?.let {
           DateConverter.dateToLocalString2(it)
        }

        binding.tvState.text = item.status
        binding.tvState.backgroundTintList = binding.root.context.getColorStateList(when{
            KhieuNaiEntity.Status.NEW.value == item.status
                    || KhieuNaiEntity.Status.PENDING.value == item.status -> R.color.primary
            KhieuNaiEntity.Status.RESOLVED.value == item.status -> R.color.green
            KhieuNaiEntity.Status.REJECTED.value == item.status -> R.color.red
            else -> R.color.primary
        })

        binding.tvComplaintUserName.isVisible = false
        binding.lyCb.isVisible = true
        binding.cbHandled.isChecked = item.status == KhieuNaiEntity.Status.RESOLVED.value
        binding.cbWaiting.isChecked = item.status == KhieuNaiEntity.Status.PENDING.value

        binding.root.setOnClickListener {
            listener?.onClickItem(item)
        }
    }
}