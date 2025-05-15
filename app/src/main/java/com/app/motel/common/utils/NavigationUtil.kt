package com.app.motel.common.utils

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.app.motel.R

// navigate fragment
fun Fragment.navigateFragmentWithSlide(fragmentId: Int, args: Bundle? = null) {
    val navOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.slide_in_right)
        .setExitAnim(R.anim.slide_out_left)
        .setPopEnterAnim(R.anim.slide_in_left)
        .setPopExitAnim(R.anim.slide_out_right)
        .build()

    findNavController().navigate(fragmentId, args, navOptions)
}

fun Fragment.popFragmentWithSlide() {
    findNavController().popBackStack()
}

fun Activity.navigateFragment(viewId: Int,fragmentId: Int, args: Bundle? = null) {
    ((this as AppCompatActivity).supportFragmentManager.findFragmentById(viewId) as NavHostFragment)
        .navController.navigate(fragmentId, args)
}

fun Activity.navigateFragmentWithSlide(viewId: Int,fragmentId: Int, args: Bundle? = null) {
    val navOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.slide_in_right)
        .setExitAnim(R.anim.slide_out_left)
        .setPopEnterAnim(R.anim.slide_in_left)
        .setPopExitAnim(R.anim.slide_out_right)
        .build()

    ((this as AppCompatActivity).supportFragmentManager.findFragmentById(viewId) as NavHostFragment)
        .navController.navigate(fragmentId, args, navOptions)
}

fun Activity.popFragmentWithSlide(viewId: Int) {
    findNavController(viewId).popBackStack()
}

// navigate activity
fun Activity.startActivityWithTransition(intent: Intent) {
    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
    startActivity(intent, options.toBundle())
}

fun Activity.startActivityWithSlide(intent: Intent, launcher: ActivityResultLauncher<Intent>? = null) {
    val options = ActivityOptions.makeCustomAnimation(
        this,
        R.anim.slide_in_right,
        R.anim.slide_out_left
    )

    if(launcher != null){
        launcher.launch(intent)
    }else{
        startActivity(intent, options.toBundle())
    }
}

fun Activity.finishActivityWithTransition() {
    finishAfterTransition()
}