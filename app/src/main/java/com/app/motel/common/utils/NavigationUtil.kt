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

    //Điều hướng đến fragment mới với hiệu ứng trượt, cho phép truyền đối số
    fun Fragment.navigateFragmentWithSlide(fragmentId: Int, args: Bundle? = null) {
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()

        findNavController().navigate(fragmentId, args, navOptions)
    }

    //Quay lại fragment trước đó trong ngăn xếp điều hướng
    fun Fragment.popFragmentWithSlide() {
        findNavController().popBackStack()
    }

    //Cho phép Activity điều hướng đến fragment mới không có hiệu ứng
    fun Activity.navigateFragment(viewId: Int,fragmentId: Int, args: Bundle? = null) {
        ((this as AppCompatActivity).supportFragmentManager.findFragmentById(viewId) as NavHostFragment)
            .navController.navigate(fragmentId, args)
    }

    //Cho phép Activity điều hướng đến fragment mới với hiệu ứng trượt
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

    //Cho phép Activity quay lại fragment trước đó trong ngăn xếp điều hướng
    fun Activity.popFragmentWithSlide(viewId: Int) {
        findNavController(viewId).popBackStack()
    }

    // navigate activity

    //Khởi động Activity mới với hiệu ứng chuyển cảnh mặc định
    fun Activity.startActivityWithTransition(intent: Intent) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
        startActivity(intent, options.toBundle())
    }

    //Khởi động Activity mới với hiệu ứng trượt, có thể sử dụng ActivityResultLauncher để nhận kết quả trả về
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

    //Kết thúc Activity hiện tại với hiệu ứng chuyển cảnh
    fun Activity.finishActivityWithTransition() {
        finishAfterTransition()
    }