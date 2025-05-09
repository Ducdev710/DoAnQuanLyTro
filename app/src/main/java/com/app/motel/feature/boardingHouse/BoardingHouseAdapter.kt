package com.app.motel.feature.boardingHouse

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.app.motel.R
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.model.BoardingHouse
import com.app.motel.databinding.ItemBoardingHouseBinding

class BoardingHouseAdapter(
    private val listener: AppListener<BoardingHouse>
): AppBaseAdapter<BoardingHouse, ItemBoardingHouseBinding>() {

    private var currentBoardingHouse: BoardingHouse? = null
    @SuppressLint("NotifyDataSetChanged")
    fun updateCurrentBoardingHouse(boardingHouse: BoardingHouse?){
        currentBoardingHouse = boardingHouse
        notifyDataSetChanged()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemBoardingHouseBinding {
         return ItemBoardingHouseBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun bind(binding: ItemBoardingHouseBinding, item: BoardingHouse, position: Int) {
        if(currentBoardingHouse?.id == item.id) binding.root.setBackgroundResource(R.drawable.background_border_radius_border_1)
        else binding.root.setBackgroundResource(R.drawable.background_border_radius_1)
        binding.iconSelect.isVisible = currentBoardingHouse?.id == item.id

        if(currentBoardingHouse?.id == item.id) binding.tvTotalRoom.setBackgroundResource(R.drawable.background_border_radius_1)
        else binding.tvTotalRoom.setBackgroundResource(R.drawable.background_border_radius_3)

        binding.tvName.text = item.name
        binding.tvAddress.text = item.address
        binding.tvTotalRoom.text = "Tổng số phòng: ${item.rooms?.size ?: 0}"
        binding.tvTotalRoomEmpty.text = "${item.getRoomEmpty?.size ?: 0} phòng trống"

        binding.root.setOnClickListener {
            listener.onClickItem(item, ItemAction.CLICK)
        }
        binding.btnDelete.setOnClickListener {
            listener.onClickItem(item, ItemAction.DELETE)
        }
        binding.btnManagement.setOnClickListener {
            listener.onClickItem(item, ItemAction.EDIT)
        }
    }
}