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

class HandleContractAdapter(
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

            tvState.text = "Tình trạng hợp đồng: ${item.state.value}"
            tvState.setTextColor(binding.root.context.resources.getColor(R.color.green, binding.root.context.theme))

            tvDateCreate.text = item.createdDate
            tvDateStart.text = item.startDate
            tvDateEnd.text = item.endDate

            btnEnd.isVisible = (item.state == Contract.State.ACTIVE || item.state == Contract.State.NEAR_END || item.state == Contract.State.ENDED) && item.isActive == HopDongEntity.ACTIVE
            btnRefresh.isVisible = (item.state == Contract.State.NEAR_END || item.state == Contract.State.ENDED || item.state == Contract.State.UNKNOWN) && item.isActive == HopDongEntity.ACTIVE
            spaceBtn.isVisible = btnEnd.isVisible && btnRefresh.isVisible

            root.setOnClickListener{
                listener.onClickItem(item, ItemAction.CLICK)
            }
            btnEnd.setOnClickListener{
                listener.onClickItem(item, ItemAction.DELETE)
            }
            btnRefresh.setOnClickListener{
                listener.onClickItem(item, ItemAction.EDIT)
            }
        }

    }
}