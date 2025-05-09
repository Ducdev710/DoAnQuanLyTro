package com.app.motel.feature.room

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.app.motel.common.ultis.toStringMoney
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.model.Room
import com.app.motel.databinding.ItemRoomContractBinding

class RoomAdapter (
    private val onListener: AppListener<Room>? = null,
): AppBaseAdapter<Room, ItemRoomContractBinding>() {

    private var showBtnRentRoom: Boolean = false
    @SuppressLint("NotifyDataSetChanged")
    fun setShowButtonRentRoom(show: Boolean){
        showBtnRentRoom = show
        notifyDataSetChanged()
    }

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
        binding.tvNameBoardingHouse.isVisible = showBtnRentRoom
        binding.tvNameBoardingHouse.text = "Khu ${item.boardingHouse?.name?.replace("Khu", "")?.replace("khu", "") ?: "Không rõ"}"
        binding.tvQuantity.text = "Số lượng người tối đa: ${item.maxOccupants ?: 0} \tSố người đang ở: ${item.tenants?.size ?: 0}"
        binding.tvPrice.text = item.rentalPrice.toStringMoney()
        binding.cbEmpty.isChecked = item.isEmpty
        binding.cbLiving.isChecked = item.isRenting

        binding.btnRent.isVisible = showBtnRentRoom && item.isEmpty
        binding.btnRent.setOnClickListener {
            onListener?.onClickItem(item, ItemAction.CUSTOM)
        }
    }
}