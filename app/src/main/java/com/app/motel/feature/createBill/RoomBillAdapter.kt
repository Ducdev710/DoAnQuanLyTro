package com.app.motel.feature.createBill

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.model.Room
import com.app.motel.databinding.ItemRoomContractBinding

class RoomBillAdapter (
    private val onListener: AppListener<Room>? = null,
): AppBaseAdapter<Room, ItemRoomContractBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemRoomContractBinding {
        return ItemRoomContractBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemRoomContractBinding, item: Room, position: Int) {
        binding.tvQuantity.isVisible = false
        binding.cbLiving.isVisible = false  // replace with cbEmpty to get theme that checkbox

        binding.root.setOnClickListener{
            onListener?.onClickItem(item)
        }
        binding.tvName.text = item.roomName
        binding.tvPrice.text = item.rentalPrice
        binding.cbEmpty.text = "Đang ở"
        binding.cbEmpty.isChecked = item.isRenting
    }
}