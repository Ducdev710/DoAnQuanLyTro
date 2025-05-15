package com.app.motel.feature.boardingHouse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.motel.AppApplication
import com.app.motel.R
import com.app.motel.common.utils.navigateFragmentWithSlide
import com.app.motel.core.AppBaseFragment
import com.app.motel.databinding.FragmentGreetingBoardingHouseBinding
import javax.inject.Inject


class GreetingBoardingHouseFragment @Inject constructor() : AppBaseFragment<FragmentGreetingBoardingHouseBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentGreetingBoardingHouseBinding {
        return FragmentGreetingBoardingHouseBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as AppApplication).appComponent.inject(this)

//        viewModel.handle(HomeViewAction.GetMotelViewAction)
//        viewModel.handle(HomeViewAction.GetBoardingViewAction)
        views.btnCreate.setOnClickListener{
            navigateFragmentWithSlide(R.id.createBoardingHouseFragment)
        }

        super.onViewCreated(view, savedInstanceState)
    }

}