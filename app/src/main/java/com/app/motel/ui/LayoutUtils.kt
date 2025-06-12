package com.app.motel.ui

import android.app.Dialog
import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.app.motel.core.AppBaseDialog
import com.app.motel.databinding.DialogLoadingBinding

/**
 * Hiển thị dialog loading với các tùy chọn tùy chỉnh.
 *
 * Hàm tiện ích này tạo và hiển thị một dialog loading đơn giản với khả năng
 * tùy chỉnh nội dung hiển thị và khả năng hủy. Dialog sử dụng layout
 * dialog_loading.xml thông qua view binding.
 *
 * @param context Context để tạo dialog
 * @param layoutInflater LayoutInflater để inflate view
 * @param content Nội dung text hiển thị bên dưới biểu tượng loading (null để ẩn)
 * @param isCancelable Cho phép người dùng hủy dialog bằng cách nhấn bên ngoài hoặc nút back
 * @return Dialog đã được tạo và hiển thị, có thể sử dụng để đóng dialog sau này
 */
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