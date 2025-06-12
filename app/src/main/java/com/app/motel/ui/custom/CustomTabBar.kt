package com.app.motel.ui.custom

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.app.motel.R
import com.app.motel.databinding.LayoutTabBarBinding

class CustomTabBar(context: Context?, attrs: AttributeSet?) :
    RelativeLayout(context, attrs) {

        //Định nghĩa callback interface để thông báo khi tab được chọn
        interface OnTabSelectedListener {
            fun onTabSelected(position: Int)
        }

        private var tabSelectedListener: OnTabSelectedListener? = null
        fun setOnTabSelectedListener(listener: OnTabSelectedListener) {
            tabSelectedListener = listener
        }

        private var binding: LayoutTabBarBinding = LayoutTabBarBinding
            .inflate(LayoutInflater.from(context), this, true)
        private lateinit var listTabName: List<String>
        private lateinit var listTabTv: List<TextView>
        var currentPosition = 0
        init {
            setupAttrs(attrs)
            setupUI()
        }

        fun setTabs(tabs: List<String>) {
            listTabName = tabs
            setupUI()
        }

        //Đọc thuộc tính entries từ XML thông qua TypedArray
        //Chuyển đổi mảng các tên tab thành List<String>
        //Giải phóng TypedArray khi hoàn thành
        private fun setupAttrs(attrs: AttributeSet?) {
            val typedArray = context.theme.obtainStyledAttributes(
                attrs, R.styleable.CustomTabBar,
                0, 0)

            listTabName = typedArray
                .getTextArray(R.styleable.CustomTabBar_android_entries)
                .toList().map {
                    it.toString()
                }

            typedArray.recycle()
        }

        private fun setupUI() {
            // Xóa các tab cũ
            binding.viewTabsWrapper.removeAllViews()

            //Tạo TextView cho từng tab
            listTabTv = listTabName.mapIndexed { position, tabName ->
                initTabTv(tabName, position)
            }

            //Thiết lập container cho các tab
            binding.viewTabsWrapper.apply {
                weightSum = listTabTv.size.toFloat()
                listTabTv.forEach {
                    this.addView(it)
                }
            }

            //Thiết lập container cho indicator
            binding.viewIndicatorWrapper.apply {
                //weight sum = number of tabs
                weightSum = listTabTv.size.toFloat()
            }
        }

        private fun initTabTv(tabName: String, position: Int) = TextView(context).apply {
            text = tabName
            layoutParams = LinearLayout.LayoutParams(
                0,
                LayoutParams.MATCH_PARENT,
                1f
            )
            gravity = Gravity.CENTER
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = true
            setPadding(2, 2, 2, 2)

            // Thiết lập màu ban đầu
            setTextColor(
                ContextCompat.getColor(
                    this.context,
                    if (position == currentPosition) R.color.white else R.color.textColor2
                )
            )

            setOnClickListener {
                setTabSelected(position)
            }
        }

        fun setTabSelected(position: Int) {
            tabSelectedListener?.onTabSelected(position)

            ObjectAnimator.ofFloat(
                binding.viewIndicator,
                View.TRANSLATION_X,
                binding.viewIndicator.x,
                listTabTv[position].x
            ).apply {
                duration = 300
                start()
            }

            currentPosition = position
            listTabTv.forEachIndexed { i, textView ->
                textView.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (i == position) R.color.white else R.color.textColor2
                    )
                )
            }
        }

}