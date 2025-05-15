package com.app.motel.common.utils

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.app.motel.core.AppBaseDialog
import com.app.motel.data.model.Resource
import com.app.motel.databinding.DialogConfirmBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern

inline fun <T> LiveData<Resource<T>>.observe(
    owner: LifecycleOwner,
    crossinline onChanged: (Resource<T>) -> Unit
) {
    observe(owner) { resource ->
        resource.getDataIfNotHandled()?.let { onChanged(it) }
    }
}

fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: Observer<T>) {
    observe(owner, object : Observer<T> {
        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this)
        }
    })
}

fun String.require(): CharSequence{
    return SpannableStringBuilder().apply {
        append(this@require)
        append(" *", ForegroundColorSpan(Color.RED), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

fun String?.toMoney(): Int{
    return this
        ?.replace(",", "")
        ?.replace(" ", "")
        ?.trim()
        ?.toIntOrNull() ?: 0
}

fun String?.toStringMoney(): String{
    return try {
        val money = this.toMoney()
        if(money == 0) return this ?: ""

        val symbols = DecimalFormatSymbols().apply {
            groupingSeparator = ' '
        }
        val formatter = DecimalFormat("#,###", symbols)
        formatter.format(money)
    } catch (e: Exception) {
        "0"
    }
}

fun Any?.toStringMoney(): String {
    return try {
        val number = when (this) {
            is Number -> this.toDouble()
            is String -> this.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
        DecimalFormat("#,###").format(number)
    } catch (e: Exception) {
        "0"
    }
}


fun Int.formatRoomName(): String {
    return if (this < 100) {
        (this + 100).toString()
    } else {
        this.toString()
    }
}

fun Context.showToast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.showDialogConfirm(
    title: String,
    content: String?,
    buttonConfirm: String = "Có",
    buttonCancel: String = "Hủy",
    confirm: (() -> Unit) = {},
    cancel: (() -> Unit) = {}
){
    val dialog = AppBaseDialog
        .Builder(this, DialogConfirmBinding.inflate(LayoutInflater.from(this)))
        .build()
    dialog.show()

    dialog.binding.apply {
        tvTitle.text = title
        tvContent.text = content
        tvConfirm.text = buttonConfirm
        tvCancel.text = buttonCancel

        tvContent.isVisible = !content.isNullOrEmpty()

        tvConfirm.setOnClickListener {
            confirm()
            dialog.dismiss()
        }
        tvCancel.setOnClickListener {
            cancel()
            dialog.dismiss()
        }
    }
}

fun String.containsSearch(str: String): Boolean {
    val normalized1 = Normalizer.normalize(this, Normalizer.Form.NFD)
    val normalized2 = Normalizer.normalize(str, Normalizer.Form.NFD)
    val text1String = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        .matcher(normalized1)
        .replaceAll("")
        .lowercase(Locale.getDefault())
  val text2String = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        .matcher(normalized2)
        .replaceAll("")
        .lowercase(Locale.getDefault())

    return text1String.contains(text2String)
}