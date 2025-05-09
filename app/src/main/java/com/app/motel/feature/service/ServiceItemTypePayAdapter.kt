package com.app.motel.feature.service

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.entity.DichVuEntity
import com.app.motel.databinding.ItemServiceTypePayBinding

class ServiceItemTypePayAdapter (
    private var currentItem: DichVuEntity.TypePay?,
): AppBaseAdapter<DichVuEntity.TypePay, ItemServiceTypePayBinding>() {
    val getCurrentItem: DichVuEntity.TypePay get () = currentItem ?: DichVuEntity.TypePay.entries.first()
    init {
        updateData(DichVuEntity.TypePay.entries)
    }
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemServiceTypePayBinding {
        return ItemServiceTypePayBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun bind(binding: ItemServiceTypePayBinding, item: DichVuEntity.TypePay, position: Int) {
        binding.radioItem.text = item.typeName
        binding.radioItem.isChecked = currentItem == item
        binding.radioItem.setOnClickListener {
            currentItem = item
            notifyDataSetChanged()
        }
        binding.root.setOnClickListener {
            currentItem = item
            notifyDataSetChanged()
        }
    }

}