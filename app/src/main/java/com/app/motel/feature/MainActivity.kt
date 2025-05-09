package com.app.motel.feature

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.app.motel.R
import com.app.motel.AppApplication
import com.app.motel.core.AppBaseActivity
import com.app.motel.databinding.ActivityMainBinding
import com.app.motel.feature.home.viewmodel.HomeViewEvent
import com.app.motel.feature.home.viewmodel.HomeViewModel
import com.app.motel.common.ultis.navigateFragment
import com.app.motel.common.ultis.startActivityWithTransition
import com.app.motel.data.model.Resource
import com.app.motel.feature.boardingHouse.BoardingHouseActivity
import com.app.motel.feature.auth.AuthActivity
import javax.inject.Inject

class MainActivity : AppBaseActivity<ActivityMainBinding>() {

    override fun getBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mViewModel : HomeViewModel by lazy{
        ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as AppApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(views.root)

        mViewModel.observeViewEvents {
            when(it){
                is HomeViewEvent.ReturnTestViewEvent ->  Log.e("TAG", "Test View event")
                else -> {}
            }
        }
        setUpBottomNav()
        setupToolBar()
        handleObserverData()
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun setUpBottomNav() {
        val navController = this.findNavController(R.id.fragment_view)
        views.navBottom.setupWithNavController(navController)

        views.navBottom.menu.clear()
        views.navBottom.inflateMenu(
            if (mViewModel.userController.state.getCurrentUser?.isAdmin == true)
                R.menu.menu_bottom_admin else R.menu.menu_bottom_user
        )
        this.findNavController(R.id.fragment_view).currentDestination?.id?.apply {
            views.navBottom.selectedItemId = this
        }
        this.findNavController(R.id.fragment_view).addOnDestinationChangedListener { controller, destination, arguments ->
            views.navBottom.isVisible = destination.id != R.id.boardingHouseListFragment
        }
    }

    private fun setupToolBar(){
        (views.toolbar.context as AppCompatActivity).setSupportActionBar(views.toolbar)
        views.toolbar.setNavigationIcon(R.drawable.baseline_home_24)
        views.toolbar.navigationIcon?.setTint(ContextCompat.getColor(baseContext, R.color.white))
        views.toolbar.setTitleTextColor(ContextCompat.getColor(baseContext, R.color.white))
        views.toolbar.setSubtitleTextColor(ContextCompat.getColor(baseContext, R.color.white))
        views.toolbar.overflowIcon?.setTint(ContextCompat.getColor(baseContext, R.color.white))
        views.toolbar.setTitleTextAppearance(baseContext, R.style.ToolbarTitleStyle)
        views.toolbar.isTitleCentered = true
        views.toolbar.setNavigationOnClickListener {
            navigateFragment(R.id.fragment_view, R.id.nav_home,)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(mViewModel.userController.state.getCurrentUser?.isAdmin == true){
            menuInflater.inflate(R.menu.menu_action_toolbar, menu)
        }

        val icon = menu?.findItem(R.id.action_menu)?.icon
        icon?.mutate()?.setTint(ContextCompat.getColor(baseContext, R.color.white))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_menu -> {
                navigateFragment(R.id.fragment_view, R.id.boardingHouseListFragment,)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun init() {
        mViewModel.userController.getCurrentUser()
    }

    private fun handleObserverData() {
        mViewModel.userController.state.currentUser.observe(this){
            Log.e("MainActivity", "currentUser: ${it.data?.role}")
            if(it.isError()){
                mViewModel.logout()
                finishAffinity()
                startActivity(Intent(this, AuthActivity::class.java))
            }
            setUpBottomNav()
            invalidateOptionsMenu()
        }

        mViewModel.userController.state.currentBoardingHouse.observe(this){
            if(it.isSuccess()){
                mViewModel.getBoardingById(it?.data?.id)
            }
            if(it.isError()){
                mViewModel.userController.state.currentBoardingHouse.postValue(Resource.Initialize())
                startActivityWithTransition(Intent(this, BoardingHouseActivity::class.java))
                finishAffinity()
            }
        }
    }

}