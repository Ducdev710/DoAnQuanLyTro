package com.app.motel.common.ultis

import android.annotation.SuppressLint
import android.view.MotionEvent
import com.google.android.material.textfield.TextInputEditText

@SuppressLint("ClickableViewAccessibility")
fun TextInputEditText.setOnEndDrawableClick(listener: () -> Unit) {
    this.setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            val drawableEnd = this.compoundDrawables[2]
            drawableEnd?.let {
                val touchAreaStart = this.width - this.paddingEnd - it.bounds.width()
                if (event.x >= touchAreaStart) {
                    listener()
                    return@setOnTouchListener true
                }
            }
        }
        false
    }
}