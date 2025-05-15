package com.app.motel.feature.createContract

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
import com.app.motel.databinding.ActivityCreateContractBinding
import com.app.motel.feature.createContract.viewmodel.CreateContractViewModel
import javax.inject.Inject


class CreateContractActivity() : AppBaseActivity<ActivityCreateContractBinding>() {

    companion object{
        const val KEY_TENANT_ID = "KEY_TENANT_ID"
        const val KEY_ROOM_ID = "KEY_ROOM_ID"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : CreateContractViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(CreateContractViewModel::class.java)
    }

    override fun getBinding(): ActivityCreateContractBinding {
        return ActivityCreateContractBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        (application as AppApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(views.root)

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
            val isHomeFragment = destination.id == R.id.creatContractListFragment
            supportActionBar?.setDisplayShowTitleEnabled(isHomeFragment)
        }

        val roomId: String? = intent.getStringExtra(KEY_ROOM_ID)
        val tenantId: String? = intent.getStringExtra(KEY_TENANT_ID)
        viewModel.initForm(roomId, tenantId)
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
        supportActionBar?.title = "Lập hợp đồng mới"
        views.toolbar.setNavigationOnClickListener {
            handleBackWithAnimation()
        }
    }

    private fun handleBackWithAnimation() {
        val navController = findNavController(R.id.fragment_view)

        if (navController.currentDestination?.id == R.id.creatContractListFragment) {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        } else {
            popFragmentWithSlide(R.id.fragment_view)
        }
    }
}