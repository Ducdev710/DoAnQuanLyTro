package com.app.motel.feature.tenant

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.model.Room
import com.app.motel.data.model.Tenant
import com.app.motel.databinding.ItemTenantBinding

class TenantAdapter(
    val listener: AppBaseAdapter.AppListener<Tenant>
): AppBaseAdapter<Tenant, ItemTenantBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemTenantBinding {
        return ItemTenantBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemTenantBinding, item: Tenant, position: Int) {
        binding.tvRoomName.text = item.room?.roomName
        binding.tvName.text = "Họ tên: \t${item.fullName}"
        binding.tvNumberPhone.text = "SĐT: \t${item.phoneNumber ?: "Không có"}"

        binding.cbRenting.isChecked = item.room != null
        binding.cbRentingMain.isChecked = item.id == item.contract?.customerId

        binding.btnCall.setOnClickListener {
            item.phoneNumber?.let { phoneNumber ->
                if (phoneNumber.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phoneNumber")
                    }
                    it.context.startActivity(intent)
                } else {
                    Toast.makeText(it.context, "Không có số điện thoại", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(it.context, "Không có số điện thoại", Toast.LENGTH_SHORT).show()
            }
        }

        binding.root.setOnClickListener {
            listener.onClickItem(item, ItemAction.CLICK)
        }
        binding.root.setOnLongClickListener {
            listener.onClickItem(item, ItemAction.LONG_CLICK)
            true
        }
    }
}