package com.app.motel.feature.rules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.common.service.IDManager
import com.app.motel.common.utils.showToast
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseDialog
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Rules
import com.app.motel.databinding.DialogAddRulesBinding
import com.app.motel.databinding.FragmentRuleFormBinding
import com.app.motel.feature.rules.viewmodel.RulesViewModel
import javax.inject.Inject

class RuleFormFragment @Inject constructor() : AppBaseFragment<FragmentRuleFormBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRuleFormBinding {
        return FragmentRuleFormBinding.inflate(inflater, container, false)
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
    }

    //Khởi tạo adapter với listener xử lý click vào item để sửa nội quy
    var adapter: RulesAdapter = RulesAdapter(object : AppBaseAdapter.AppListener<Rules>() {
        override fun onClickItem(item: Rules, action: AppBaseAdapter.ItemAction) {
            showAddDialog(item)
        }
    })

    //Tải danh sách nội quy từ ViewModel
    private fun init() {
        viewModel.getRules()
        views.rcv.adapter = adapter

        views.btnSave.setOnClickListener {
            viewModel.saveRules(viewModel.liveData.getAllRules)
        }

        views.btnAdd.setOnClickListener {
            showAddDialog()
        }
    }

    //Quan sát rules LiveData và cập nhật adapter khi có dữ liệu mới
    //Hiển thị thông báo thành công khi lưu thành công
    //Tải lại danh sách nội quy sau khi lưu
    private fun listenStateViewModel() {
        viewModel.liveData.rules.observe(viewLifecycleOwner){
            val rules: List<Rules> = viewModel.liveData.getAllRulesActive

            adapter.updateData(rules)
            views.tvEmpty.isVisible = rules.isEmpty()
        }
        viewModel.liveData.saveRules.observe(viewLifecycleOwner){
            if(it.isSuccess()){
                requireActivity().showToast("Lưu thành công")
                viewModel.getRules()
            }
        }
    }

    //Sử dụng tham số rules để xác định:
    //Nếu null: Thêm mới nội quy (tạo ID mới)
    //Nếu không null: Cập nhật nội quy hiện có
    private fun showAddDialog(rules: Rules? = null){
        val dialog = AppBaseDialog.Builder(requireContext(), DialogAddRulesBinding.inflate(layoutInflater))
            .isBorderRadius(false)
            .build()
        dialog.show()

        val rulesSave: Rules = rules?.copy() ?: Rules(
            id = IDManager.createID(),
            title = "",
            content = "",
            boardingHouseId = "",
        )

        dialog.binding.apply {
            txtTitle.setText(rulesSave.title)
            txtContent.setText(rulesSave.content)
            btnDelete.isVisible = rules != null

            btnSave.setOnClickListener {
                viewModel.handleRules(rulesSave.copy(
                    title = txtTitle.text.toString(),
                    content = txtContent.text.toString(),
                ))
            }
            btnDelete.setOnClickListener {
                viewModel.handleRules(rulesSave, false)
            }
        }

        viewModel.liveData.addRules.observe(dialog){
            if(it.isSuccess()){
                dialog.dismiss()
            }else if(it.isError()){
                dialog.context.showToast(it.message ?: "Có lỗi xảy ra")
            }
        }

        dialog.setOnDismissListener {
            viewModel.liveData.addRules.postValue(Resource.Initialize())
        }
    }

}