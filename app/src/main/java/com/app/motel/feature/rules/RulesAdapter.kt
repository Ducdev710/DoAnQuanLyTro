package com.app.motel.feature.rules

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.model.Rules
import com.app.motel.databinding.ItemRulesBinding

class RulesAdapter(
    private val listener: AppBaseAdapter.AppListener<Rules>
): AppBaseAdapter<Rules, ItemRulesBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemRulesBinding {
         return ItemRulesBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemRulesBinding, item: Rules, position: Int) {
        binding.tvTitle.text = item.title
        binding.tvContent.text = item.content

        binding.root.setOnClickListener{
            listener.onClickItem(item, ItemAction.CLICK)
        }
    }
}