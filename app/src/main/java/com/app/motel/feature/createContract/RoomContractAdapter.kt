package com.app.motel.feature.createContract

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.motel.common.ultis.toStringMoney
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.model.Room
import com.app.motel.databinding.ItemRoomContractBinding

class RoomContractAdapter (
    private val onListener: AppListener<Room>? = null,
): AppBaseAdapter<Room, ItemRoomContractBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemRoomContractBinding {
        return ItemRoomContractBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemRoomContractBinding, item: Room, position: Int) {
        binding.root.setOnClickListener{
            onListener?.onClickItem(item)
        }
        binding.tvName.text = item.roomName
        binding.tvQuantity.text = "Số lượng người tối đa: ${item.maxOccupants ?: 0} \tSố người đang ở: ${item.tenants?.size ?: 0}"
        binding.tvPrice.text = item.rentalPrice.toStringMoney()
        binding.cbEmpty.isChecked = item.isEmpty
        binding.cbLiving.isChecked = item.isRenting
    }
}