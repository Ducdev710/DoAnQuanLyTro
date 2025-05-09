package com.app.motel.feature.boardingHouse

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.ultis.popFragmentWithSlide
import com.app.motel.core.AppBaseActivity
import com.app.motel.data.model.BoardingHouse
import com.app.motel.databinding.ActivityBoardingHouseBinding
import com.app.motel.feature.boardingHouse.viewmodel.BoardingHouseViewModel
import com.google.gson.Gson
import javax.inject.Inject

class BoardingHouseActivity : AppBaseActivity<ActivityBoardingHouseBinding>() {

    override fun getBinding(): ActivityBoardingHouseBinding {
         return ActivityBoardingHouseBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel : BoardingHouseViewModel by lazy{
        ViewModelProvider(this, viewModelFactory).get(BoardingHouseViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as AppApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(views.root)

        init()
    }

    fun init(){
        val boardingHouse: BoardingHouse? = Gson().fromJson(intent.getStringExtra(KEY_BOARDING_HOUSE), BoardingHouse::class.java)
        viewModel.initForm(boardingHouse)

        if(boardingHouse != null){
            val navController = findNavController(R.id.fragment_view)
            val navInflater = navController.navInflater
            val graph = navInflater.inflate(R.navigation.nav_boarding_house)
            graph.setStartDestination(R.id.createBoardingHouseFragment)
            navController.graph = graph
        }
    }

    companion object{
        const val KEY_BOARDING_HOUSE = "KEY_BOARDING_HOUSE"
    }
}