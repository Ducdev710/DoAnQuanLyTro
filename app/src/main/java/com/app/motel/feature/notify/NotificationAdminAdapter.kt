package com.app.motel.feature.notify

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.motel.R
import com.app.motel.common.service.DateConverter
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.entity.KhieuNaiEntity
import com.app.motel.data.model.Complaint
import com.app.motel.databinding.ItemComplaintBinding

class NotificationAdminAdapter(
    val listener: AppBaseAdapter.AppListener<Complaint>
): AppBaseAdapter<Complaint, ItemComplaintBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemComplaintBinding {
        return ItemComplaintBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun bind(binding: ItemComplaintBinding, item: Complaint, position: Int) {
        binding.tvRoomName.text = "Phòng: ${item.room?.roomName ?: ""}"
        binding.tvStatus.text = item.status

        binding.tvComplaintUserName.text = item.tenant?.fullName
        binding.tvTitle.text = item.title
        binding.tvContent.text = item.content
        binding.tvCreateDate.text = DateConverter.stringToDate(item.createdDate ?: "")?.let {
            DateConverter.dateToLocalString2(it)
        }

        // Xử lý màu sắc trạng thái
        binding.tvState.text = item.status
        binding.tvState.backgroundTintList = binding.root.context.getColorStateList(when{
            KhieuNaiEntity.Status.NEW.value == item.status
                    || KhieuNaiEntity.Status.PENDING.value == item.status -> R.color.primary
            KhieuNaiEntity.Status.RESOLVED.value == item.status -> R.color.green
            KhieuNaiEntity.Status.REJECTED.value == item.status -> R.color.red
            else -> R.color.primary
        })

        binding.root.setOnClickListener {
            // Nếu là thông báo hệ thống và đang ở trạng thái "Mới", tự động đánh dấu đã đọc
            if (item.type == KhieuNaiEntity.Type.APPLICATION.value &&
                item.status == KhieuNaiEntity.Status.NEW.value) {
                // Tạo bản sao của item với trạng thái đã cập nhật
                val updatedItem = item.copy(status = KhieuNaiEntity.Status.RESOLVED.value)
                // Gọi sự kiện để cập nhật vào database
                listener.onClickItem(updatedItem, ItemAction.UPDATE_STATUS)
            }

            // Vẫn gọi sự kiện click thông thường
            listener.onClickItem(item)
        }

        // Xử lý sự kiện long click cho các khiếu nại và yêu cầu thuê phòng
        if (item.type == KhieuNaiEntity.Type.COMPLAINT.value || item.type == KhieuNaiEntity.Type.RENT_ROOM.value) {
            binding.root.setOnLongClickListener {
                listener.onClickItem(item, ItemAction.LONG_CLICK)
                true
            }
        } else {
            // Xóa sự kiện long click nếu là thông báo hệ thống
            binding.root.setOnLongClickListener(null)
        }
    }
}