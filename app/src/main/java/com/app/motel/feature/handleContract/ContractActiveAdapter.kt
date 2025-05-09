package com.app.motel.feature.handleContract

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.app.motel.R
import com.app.motel.core.AppBaseAdapter
import com.app.motel.data.entity.HopDongEntity
import com.app.motel.data.model.Contract
import com.app.motel.databinding.ItemHandleContractBinding

class ContractActiveAdapter(
    val listener: AppBaseAdapter.AppListener<Contract>
) : AppBaseAdapter<Contract, ItemHandleContractBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemHandleContractBinding {
        return ItemHandleContractBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("ResourceAsColor")
    override fun bind(binding: ItemHandleContractBinding, item: Contract, position: Int) {
        binding.apply{
            tvRoomName.text = item.name
            tvTanantName.text = "Họ và tên: ${item.tenant?.fullName ?: ""}"

            if(item.isActive == HopDongEntity.ACTIVE){
                tvState.text = "Tình trạng hợp đồng: ${item.state.value}"
                tvState.setTextColor(binding.root.context.resources.getColor(R.color.green, binding.root.context.theme))
            }else{
                tvState.text = "Tình trạng hợp đồng: hết hiệu lực"
                tvState.setTextColor(binding.root.context.resources.getColor(R.color.red, binding.root.context.theme))
            }

            tvDateCreate.text = item.createdDate
            tvDateStart.text = item.startDate
            tvDateEnd.text = item.endDate

            btnEnd.isVisible = false
            spaceBtn.isVisible = false
            btnRefresh.isVisible = false

            root.setOnClickListener{
                listener.onClickItem(item, ItemAction.CLICK)
            }
        }

    }
}