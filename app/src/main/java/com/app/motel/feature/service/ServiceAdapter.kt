package com.app.motel.feature.service

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.motel.common.ultis.toStringMoney
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.model.Service
import com.app.motel.databinding.ItemServiceBinding

class ServiceAdapter (
    private val onListener: AppListener<Service>? = null,
): AppBaseAdapter<Service, ItemServiceBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemServiceBinding {
        return ItemServiceBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun bind(binding: ItemServiceBinding, item: Service, position: Int) {
        binding.root.setOnClickListener{
            onListener?.onClickItem(item)
        }
        binding.btnDelete.setOnClickListener{
            onListener?.onClickItem(item, action = ItemAction.DELETE)
        }
        binding.tvName.text = item.name
        binding.tvPrice.text = item.getPriceTypePay
        binding.tvApply.text = if(item.isAppliesAllRoom) "Áp dụng cho tất cả phòng" else "Áp dụng cho một số phòng"
    }
}