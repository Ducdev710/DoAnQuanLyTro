package com.app.motel.feature.handleBill

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.entity.HoaDonEntity
import com.app.motel.data.model.Bill
import com.app.motel.data.model.Tenant
import com.app.motel.databinding.ItemBillBinding
import com.app.motel.databinding.ItemTenantBinding

class BillAdapter(
    val listener: AppBaseAdapter.AppListener<Bill>
): AppBaseAdapter<Bill, ItemBillBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemBillBinding {
        return ItemBillBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun bind(binding: ItemBillBinding, item: Bill, position: Int) {
        binding.tvRoomName.text = item.room?.roomName
        binding.tvMonthText.text = "Tiền thu tháng ${item.month}"
        binding.tvMonth.text = "T${item.month}"
        binding.tvYear.text = item.year.toString()
        binding.cbPayed.isChecked = item.status == HoaDonEntity.STATUS_PAID
        binding.tvTotal.text = item.totalAmount
        binding.tvCollected.text = if(item.status == HoaDonEntity.STATUS_PAID) item.totalAmount else "0"
        binding.tvRemaining.text = if(item.status == HoaDonEntity.STATUS_UNPAID) item.totalAmount else "0"

        binding.root.setOnClickListener {
            listener.onClickItem(item, ItemAction.CLICK)
        }
        binding.cbPayed.setOnClickListener {
            listener.onClickItem(item, ItemAction.CLICK)
        }
    }
}