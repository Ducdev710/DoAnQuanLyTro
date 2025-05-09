package com.app.motel.core

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class AppBaseAdapter<T, VB : ViewBinding>(
): RecyclerView.Adapter<AppBaseAdapter.BaseViewHolder<VB>>() {

    abstract fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): VB
    abstract fun bind(binding: VB, item: T, position: Int)

    private var items: List<T> = listOf()

    @Suppress("NotifyDataSetChanged")
    open fun updateData(newItems: List<T>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val binding = inflateBinding(LayoutInflater.from(parent.context), parent)
        return BaseViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        bind(holder.binding, items[position], position)
    }

    class BaseViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

    // listener
    abstract class AppListener<T> {
        abstract fun onClickItem(item: T, action: ItemAction = ItemAction.CLICK)
    }

//    interface AppListener<T> {
//        fun onClickItem(item: T, action: ItemAction = ItemAction.ACTION_CLICK) {}
//    }

    enum class ItemAction {
        CLICK,
        LONG_CLICK,
        EDIT,
        DELETE,
        SHARE,
        DOWNLOAD,
        CUSTOM
    }

}
