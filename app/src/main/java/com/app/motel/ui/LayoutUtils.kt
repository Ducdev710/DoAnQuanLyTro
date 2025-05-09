package com.app.motel.ui

import android.app.Dialog
import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.app.motel.core.AppBaseDialog
import com.app.motel.databinding.DialogLoadingBinding

fun showLoadingDialog(context: Context, layoutInflater: android.view.LayoutInflater, content: String? = null, isCancelable: Boolean = false): Dialog {
    val dialog = AppBaseDialog.Builder(context, DialogLoadingBinding.inflate(layoutInflater))
        .isWidthMatchParent(false)
        .build()
    dialog.show()
    dialog.setCancelable(isCancelable)

    val paramsProcessIndicator = dialog.binding.processIndicator.layoutParams as ViewGroup.MarginLayoutParams
    val paramsTvContent = dialog.binding.tvContent.layoutParams as ViewGroup.MarginLayoutParams
    if(content != null){
        dialog.binding.tvContent.text = content
        paramsProcessIndicator.setMargins(300, 300, 300, 150)
        paramsTvContent.setMargins(0, 0, 0, 150)

    }else{
        dialog.binding.tvContent.isVisible = false
        paramsProcessIndicator.setMargins(300, 300, 300, 300)
        paramsTvContent.setMargins(0, 0, 0, 0)
    }
    dialog.binding.processIndicator.layoutParams = paramsProcessIndicator
    dialog.binding.tvContent.layoutParams = paramsTvContent

    return dialog
}