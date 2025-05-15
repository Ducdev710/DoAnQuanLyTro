package com.app.motel.feature.room

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.utils.popFragmentWithSlide
import com.app.motel.core.AppBaseActivity
import com.app.motel.data.entity.PhongEntity
import com.app.motel.databinding.ActivityRoomBinding
import com.app.motel.feature.room.viewmodel.RoomViewModel
import javax.inject.Inject


class RoomActivity() : AppBaseActivity<ActivityRoomBinding>() {

    companion object{
        const val ROOM_STATE_KEY = "room_status"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : RoomViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(RoomViewModel::class.java)
    }

    override fun getBinding(): ActivityRoomBinding {
        return ActivityRoomBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        (application as AppApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)

        init()
        setupToolBar()
    }

    private fun init(){
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackWithAnimation()
            }
        })
        findNavController(R.id.fragment_view).addOnDestinationChangedListener { controller, destination, arguments ->
            supportActionBar?.title = when(destination.id){
                R.id.roomListFragment -> "Danh sách các phòng"
                R.id.roomDetailFragment -> "Chi tiết phòng"
                else -> "Danh sách các phòng"
            }
        }

        val status: String? = intent.getStringExtra(ROOM_STATE_KEY)
        viewModel.setStateRoomListData(status?.let { PhongEntity.Status.fromValue(status) })

    }

    private fun setupToolBar(){
        setSupportActionBar(views.toolbar)
        (views.toolbar.context as AppCompatActivity).setSupportActionBar(views.toolbar)
        views.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_new_24)
        views.toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.white))
        views.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        views.toolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.white))
        views.toolbar.overflowIcon?.setTint(ContextCompat.getColor(this, R.color.white))
        views.toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleStyle)
        views.toolbar.isTitleCentered = true
        supportActionBar?.title = "Danh sách các phòng"
        views.toolbar.setNavigationOnClickListener {
            handleBackWithAnimation()
        }
    }

    private fun handleBackWithAnimation() {
        val navController = findNavController(R.id.fragment_view)

        if (navController.currentDestination?.id == R.id.roomListFragment) {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        } else {
            popFragmentWithSlide(R.id.fragment_view)
        }
    }
}