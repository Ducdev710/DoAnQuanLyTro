package com.app.motel.feature.complaint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.utils.navigateFragmentWithSlide
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Complaint
import com.app.motel.databinding.FragmentComplaintListBinding
import com.app.motel.feature.complaint.viewmodel.ComplaintViewModel
import com.google.gson.Gson
import javax.inject.Inject

class ComplaintListFragment: AppBaseFragment<FragmentComplaintListBinding>() {
    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentComplaintListBinding {
         return FragmentComplaintListBinding.inflate(inflater, container, false)
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
        viewmodel.getComplaint()

        adapter = ComplaintAdapter(object : AppBaseAdapter.AppListener<Complaint>(){
            override fun onClickItem(item: Complaint, action: AppBaseAdapter.ItemAction) {
                navigateFragmentWithSlide(R.id.complaintFormFragment, Bundle().apply {
                    putString(ComplaintFormFragment.KEY_COMPLAINT, Gson().toJson(item))
                })
            }
        })
        views.rcv.adapter = adapter
        views.btnAdd.setOnClickListener{
            navigateFragmentWithSlide(R.id.complaintFormFragment)
        }
    }

    lateinit var adapter: ComplaintAdapter
    private fun listenStateViewmodel() {
        viewmodel.liveData.complains.observe(viewLifecycleOwner){
            adapter.updateData(it)
            views.tvEmpty.isVisible = it.isEmpty()
        }
    }

}