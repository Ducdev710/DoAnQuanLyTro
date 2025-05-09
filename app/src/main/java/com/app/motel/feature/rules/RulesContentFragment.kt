package com.app.motel.feature.rules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.motel.AppApplication
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Rules
import com.app.motel.databinding.FragmentRulesContentBinding
import com.app.motel.feature.rules.viewmodel.RulesViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


class RulesContentFragment @Inject constructor() : AppBaseFragment<FragmentRulesContentBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRulesContentBinding {
        return FragmentRulesContentBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : RulesViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(RulesViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        super.onViewCreated(view, savedInstanceState)
        init()
        listenStateViewModel()
        loadLandlordInfo()
    }

    var adapter: RulesAdapter = RulesAdapter(object : AppBaseAdapter.AppListener<Rules>() {
        override fun onClickItem(item: Rules, action: AppBaseAdapter.ItemAction) {

        }
    })

    private fun init() {
        viewModel.getRules()

        views.rcv.adapter = adapter
    }

    private fun listenStateViewModel() {
        viewModel.liveData.rules.observe(viewLifecycleOwner){
            val rules: List<Rules> = viewModel.liveData.getAllRulesActive

            adapter.updateData(rules)
            views.tvEmpty.isVisible = rules.isEmpty()
        }
    }

    private fun loadLandlordInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Get landlord info from your ViewModel or other data source
            val landlordInfo = viewModel.getLandlordInfo()

            // Update the TextViews with landlord info
            views.tvLandlordPhone.text = "Số điện thoại: ${landlordInfo.phoneNumber}"
            views.tvLandlordEmail.text = "Email: ${landlordInfo.email}"

            // Add bank information display
            views.txtBank.text = "Ngân hàng: ${landlordInfo.bankName ?: "Chưa cập nhật"}"
            views.txtNumberBank.text = "Số tài khoản: ${landlordInfo.accountNumber ?: "Chưa cập nhật"}"
        }
    }
}