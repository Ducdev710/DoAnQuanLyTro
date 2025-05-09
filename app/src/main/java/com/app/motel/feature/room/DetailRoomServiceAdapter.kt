package com.app.motel.feature.room

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.model.Service
import com.app.motel.databinding.ItemDetailRoomServiceBinding

class DetailRoomServiceAdapter(
    val listener: AppBaseAdapter.AppListener<Service>
): AppBaseAdapter<Service, ItemDetailRoomServiceBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemDetailRoomServiceBinding {
        return ItemDetailRoomServiceBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemDetailRoomServiceBinding, item: Service, position: Int) {
        binding.tvName.text = item.name
        binding.tvPrice.text = item.getPriceTypePay
        binding.root.setOnClickListener {
            listener.onClickItem(item, AppBaseAdapter.ItemAction.CLICK)
        }
    }
}