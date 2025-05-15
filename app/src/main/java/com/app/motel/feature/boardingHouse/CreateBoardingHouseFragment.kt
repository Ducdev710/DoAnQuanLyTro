package com.app.motel.feature.boardingHouse

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.utils.popFragmentWithSlide
import com.app.motel.common.utils.require
import com.app.motel.common.utils.showToast
import com.app.motel.common.utils.startActivityWithTransition
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Status
import com.app.motel.databinding.FragmentCreateBoardingHouseBinding
import com.app.motel.feature.boardingHouse.viewmodel.BoardingHouseViewModel
import com.app.motel.feature.MainActivity
import com.app.motel.ui.showLoadingDialog
import javax.inject.Inject

class CreateBoardingHouseFragment @Inject constructor() : AppBaseFragment<FragmentCreateBoardingHouseBinding>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : BoardingHouseViewModel by lazy{
        ViewModelProvider(requireActivity(), viewModelFactory).get(BoardingHouseViewModel::class.java)
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCreateBoardingHouseBinding {
        return FragmentCreateBoardingHouseBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        setupToolBar()
        init()
        listenDataViewModel()
        super.onViewCreated(view, savedInstanceState)
    }

    private var dialogLoading: Dialog? = null
    private fun listenDataViewModel() {
        viewModel.liveData.currentBoardingHouse.observe(viewLifecycleOwner){
            views.cbAuto.isVisible = !viewModel.liveData.isUpdateBoardingHouse
            if(it != null){
                views.txtUserName.setText(it.name)
                views.txtAddressDetail.setText(it.address)
                views.toolbar.title = "SỬA KHU TRỌ"
            }
        }
        viewModel.liveData.saveBoardingHouse.observe(viewLifecycleOwner){
            when(it.status){
                Status.LOADING -> {
                    dialogLoading = showLoadingDialog(requireContext(), layoutInflater)
                }
                Status.SUCCESS -> {
                    dialogLoading?.dismiss()
                    dialogLoading = null

                    requireContext().showToast(message = it.message ?: "Lưu thành công")
                    if(viewModel.liveData.isUpdateBoardingHouse){
                        requireActivity().finish()
                    }else{
                        requireActivity().apply {
                            startActivityWithTransition(Intent(this, MainActivity::class.java))
                            finishAffinity()
                        }
                    }
                }
                Status.ERROR -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    dialogLoading?.dismiss()
                    dialogLoading = null
                }
                Status.INITIALIZE -> {
                    dialogLoading?.dismiss()
                    dialogLoading = null
                }
            }
        }
    }

    private fun init() {
        views.tvNameBoardingHouse.text = "Tên Khu Trọ".require()
        views.tvAddressDetail.text = "Địa chỉ chi tiết".require()

        views.tvTotalRoom.isVisible = views.cbAuto.isChecked
        views.tilTotalRoom.isVisible = views.cbAuto.isChecked
        views.cbAuto.setOnClickListener {
            views.tvTotalRoom.isVisible = views.cbAuto.isChecked
            views.tilTotalRoom.isVisible = views.cbAuto.isChecked
        }

        views.btnCreate.setOnClickListener{
            viewModel.saveBoardingHouse(
                name = views.txtUserName.text.toString(),
                roomCount = views.txtTotalRoom.text.toString().toIntOrNull(),
                address = views.txtAddressDetail.text.toString(),
            )
        }
    }

    private fun setupToolBar(){
        (views.toolbar.context as AppCompatActivity).setSupportActionBar(views.toolbar)
        views.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_new_24)
        views.toolbar.navigationIcon?.setTint(ContextCompat.getColor(requireActivity(), R.color.white))
        views.toolbar.setTitleTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
        views.toolbar.setSubtitleTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
        views.toolbar.overflowIcon?.setTint(ContextCompat.getColor(requireActivity(), R.color.white))
        views.toolbar.setTitleTextAppearance(requireActivity(), R.style.ToolbarTitleStyle)
        views.toolbar.isTitleCentered = true
        views.toolbar.setNavigationOnClickListener {
            Log.e("CreateBoardingHouseFragment", "setupToolBar: ${viewModel.liveData.boardingHouse.value != null}")
            if(viewModel.liveData.isUpdateBoardingHouse) requireActivity().finish()
            else popFragmentWithSlide()
        }
    }

}