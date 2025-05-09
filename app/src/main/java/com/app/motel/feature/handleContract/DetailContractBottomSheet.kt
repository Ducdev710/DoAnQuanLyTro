package com.app.motel.feature.handleContract

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.motel.common.ultis.toStringMoney
import com.app.motel.core.AppBaseBottomSheet
import com.app.motel.data.entity.HopDongEntity
import com.app.motel.data.model.Contract
import com.app.motel.databinding.DialogDetailContractBinding

class DetailContractBottomSheet(
    private val contract: Contract
): AppBaseBottomSheet<DialogDetailContractBinding>() {
    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogDetailContractBinding {
        return DialogDetailContractBinding.inflate(inflater, container, false)
    }

    override val isExpanded: Boolean
        get() = true

    override val isBorderRadiusTop: Boolean
        get() = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        views.apply {
            tvId.text = contract.name
            tvCraeteDate.text = contract.createdDate
            tvNameRoom.text = contract.room?.roomName
            tvContractOwner.text = contract.tenant?.fullName ?: ""
            tvDuration.text = "Thời gian: ${contract.duration ?: 0} tháng"
            tvDeposit.text = "Tiền cọc: ${contract.deposit.toStringMoney()}"
            tvStartDate.text = contract.startDate
            tvStartDate.text = contract.startDate
            tvEndDate.text = contract.endDate
            txtNote.setText(contract.note)

            cbEndContract.isChecked = contract.state == Contract.State.ENDED
            cbInactive.isChecked = contract.isActive == HopDongEntity.INACTIVE

            btnEnd.setOnClickListener {
                dismiss()
            }
        }
    }
}