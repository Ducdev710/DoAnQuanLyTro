package com.app.motel.feature.complaint

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.ultis.observe
import com.app.motel.common.ultis.popFragmentWithSlide
import com.app.motel.common.ultis.showToast
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Complaint
import com.app.motel.data.model.Status
import com.app.motel.databinding.FragmentComplaintFormBinding
import com.app.motel.databinding.FragmentComplaintListBinding
import com.app.motel.feature.complaint.viewmodel.ComplaintViewModel
import com.google.gson.Gson
import javax.inject.Inject

class ComplaintFormFragment: AppBaseFragment<FragmentComplaintFormBinding>() {
    companion object{
        const val KEY_COMPLAINT = "KEY_COMPLAINT"
    }

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentComplaintFormBinding {
        return FragmentComplaintFormBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val viewmodel: ComplaintViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(ComplaintViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)

        initUI()
        listenStateViewmodel()
    }

    private fun initUI() {
        val complaint: Complaint? = arguments?.getString(KEY_COMPLAINT)?.let {
            Gson().fromJson(it, Complaint::class.java)
        }
        viewmodel.initForm(complaint)
        views.btnSave.setOnClickListener {
            viewmodel.addComplaint(views.txtTitle.text.toString(), views.txtContent.text.toString())
        }
    }

    lateinit var adapter: ComplaintAdapter
    private fun listenStateViewmodel() {
        viewmodel.liveData.currentComplain.observe(viewLifecycleOwner){
            it?.title.apply {
                views.txtTitle.setText(this)
            }
            it?.content?.apply {
                views.txtContent.setText(this)
            }
            views.btnSave.isVisible = it == null
            views.txtTitle.isEnabled = it == null
            views.txtContent.isEnabled = it == null
        }
        viewmodel.liveData.updateComplaint.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    requireActivity().showToast("Tạo đơn khiếu nại thành công")
                    popFragmentWithSlide()
                }
                Status.ERROR -> {
                    requireActivity().showToast(it.message ?: "Tạo đơn khiếu nại thất bại")
                }
                else -> {}
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewmodel.clearForm()
    }
}