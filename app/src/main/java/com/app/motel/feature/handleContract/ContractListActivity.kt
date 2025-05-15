package com.app.motel.feature.handleContract

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
import com.app.motel.databinding.ActivityContractListBinding
import com.app.motel.feature.handleContract.viewmodel.HandleContractViewModel
import javax.inject.Inject

class ContractListActivity : AppBaseActivity<ActivityContractListBinding>() {
    override fun getBinding(): ActivityContractListBinding {
         return ActivityContractListBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val mViewModel : HandleContractViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(HandleContractViewModel::class.java)
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
            val isHomeFragment = destination.id == R.id.contractListFragment
            supportActionBar?.setDisplayShowTitleEnabled(isHomeFragment)
        }
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
        supportActionBar?.title = "Danh sách hợp đồng"
        views.toolbar.setNavigationOnClickListener {
            handleBackWithAnimation()
        }
    }

    private fun handleBackWithAnimation() {
        val navController = findNavController(R.id.fragment_view)

        if (navController.currentDestination?.id == R.id.contractListFragment) {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        } else {
            popFragmentWithSlide(R.id.fragment_view)
        }
    }
}