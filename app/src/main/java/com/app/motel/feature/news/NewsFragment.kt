package com.app.motel.feature.news

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.common.utils.showToast
import com.app.motel.core.AppBaseAdapter
import com.app.motel.core.AppBaseDialog
import com.app.motel.core.AppBaseFragment
import com.app.motel.data.model.Notification
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Room
import com.app.motel.data.model.Status
import com.app.motel.databinding.DialogAddNewsBinding
import com.app.motel.databinding.FragmentNewsBinding
import com.app.motel.feature.news.viewmodel.NewsViewModel
import javax.inject.Inject

class NewsFragment : AppBaseFragment<FragmentNewsBinding>() {
    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNewsBinding {
         return FragmentNewsBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : NewsViewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory).get(NewsViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

        super.onViewCreated(view, savedInstanceState)
        init()
        listenStateViewModel()
    }
    lateinit var adapter: NewsAdapter

    private fun init() {
        viewModel.getNews()
        viewModel.getRooms()
        adapter = NewsAdapter(object: AppBaseAdapter.AppListener<Notification>(){
            override fun onClickItem(item: Notification, action: AppBaseAdapter.ItemAction) {
                when(action){
                    AppBaseAdapter.ItemAction.CLICK -> {

                    }
                    else -> {}
                }
            }
        })
        views.rcv.adapter = adapter

        views.tvAddNews.setOnClickListener{
            showAddNews()
        }
    }

    private fun listenStateViewModel() {
        viewModel.liveData.news.observe(viewLifecycleOwner){
            adapter.updateData(it)
            views.rcv.isVisible = it.isNotEmpty()
            views.tvEmpty.isVisible = it.isEmpty()
        }
        viewModel.userController.state.currentUser.observe(viewLifecycleOwner){
            views.tvUserName.text = it.data?.name
        }
        viewModel.userController.state.currentBoardingHouse.observe(viewLifecycleOwner){
            views.tvBoardingHouseName.text = it.data?.name
        }
    }

    private fun showAddNews() {
        val dialog = AppBaseDialog.Builder(
            requireContext(),
            DialogAddNewsBinding.inflate(layoutInflater)
        ).build()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        var selectedRoom: Room? = null
        setupDropDownDialog(dialog){ room ->
            selectedRoom = room
        }

        dialog.binding.apply {
            btnCancel.setOnClickListener{
                dialog.dismiss()
            }
            btnSave.setOnClickListener{
                val title = txtTitle.text.toString()
                val content = txtContent.text.toString()
                viewModel.addNews(title, content, selectedRoom?.id)
            }
        }

        viewModel.liveData.addNews.observe(dialog){
            when(it.status){
                Status.SUCCESS -> {
                    dialog.context.showToast( "Thêm tin thành công")
                    dialog.dismiss()
                    viewModel.getNews()
                }
                Status.ERROR -> {
                    dialog.context.showToast(it.message ?: "Có lỗi xảy ra")
                }
                else -> {}
            }
        }

        dialog.setOnDismissListener {
            viewModel.liveData.addNews.postValue(Resource.Initialize())
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDropDownDialog(dialog: AppBaseDialog<DialogAddNewsBinding>, function: (Room?) -> Unit) {

        val roomList = viewModel.liveData.rooms.value ?: emptyList()
        val roomNames = roomList.map { it.roomName } as ArrayList<String>
        roomNames.add(0, "Tất cả các phòng")

        dialog.window?.decorView?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (dialog.binding.dropDown.isShowing) {
                    dialog.binding.dropDown.dismiss()
                    return@setOnTouchListener true
                }
            }
            false
        }
        dialog.binding.dropDown.setItems(roomNames)
        dialog.binding.dropDown.setOnSpinnerItemSelectedListener<String> { _, _, newIndex, _ ->
            if (newIndex == 0) {
                function(null)
            } else {
                val selectedRoom = roomList[newIndex - 1]
                function(selectedRoom)
            }
        }
    }
}