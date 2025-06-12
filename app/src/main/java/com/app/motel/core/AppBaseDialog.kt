package com.app.motel.core

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.app.motel.R

/**
 * Lớp Dialog cơ sở cho toàn ứng dụng, tích hợp ViewBinding và hỗ trợ lifecycle.
 * Cung cấp khả năng tùy chỉnh giao diện dialog thông qua Builder pattern.
 *
 * @param context Context để tạo dialog
 * @param binding ViewBinding cho nội dung dialog
 * @param isBorderRadius Xác định có bo góc dialog hay không
 * @param isTransparent Xác định nền dialog có trong suốt hay không
 * @param isWidthMatchParent Xác định chiều rộng dialog có căn theo màn hình (90%) hay không
 * @param isHeightMatchParent Xác định chiều cao dialog có căn theo màn hình (95%) hay không
 * @param layoutGravity Vị trí hiển thị của dialog (trên, giữa, dưới)
 */
class AppBaseDialog<VB: ViewBinding>(
    private val context: Context,
    val binding: VB,
    private val isBorderRadius: Boolean,
    private val isTransparent: Boolean,
    private val isWidthMatchParent: Boolean,
    private val isHeightMatchParent: Boolean,
    private val layoutGravity: Int,
) : Dialog(context), LifecycleOwner {

    /**
     * Các hằng số định nghĩa vị trí hiển thị của dialog
     */
    companion object{
        const val GRAVITY_TOP: Int = Gravity.TOP
        const val GRAVITY_CENTER: Int = Gravity.CENTER
        const val GRAVITY_BOTTOM: Int = Gravity.BOTTOM
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        this.setContentView(binding.root)

        // Lấy kích thước màn hình theo cách tương thích với các phiên bản Android
        val width: Int
        val height: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = (context as Activity).windowManager.currentWindowMetrics
            val bounds = metrics.bounds
            width = bounds.width()
            height = bounds.height()
        } else {
            // Phiên bản Android cũ hơn
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            width = displayMetrics.widthPixels
            height = displayMetrics.heightPixels
        }


        // Thiết lập bo góc cho dialog nếu được yêu cầu
        if (isBorderRadius) binding.root.setBackgroundResource(R.drawable.background_border_radius_dialog)
        // Thiết lập nền trong suốt cho dialog nếu được yêu cầu
        if (isTransparent) this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        // Cấu hình kích thước và vị trí của dialog
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(this.window?.attributes)

        // Thiết lập chiều rộng: 90% màn hình hoặc wrap_content
        layoutParams.width = if (isWidthMatchParent) (width * 0.9).toInt() else WRAP_CONTENT

        // Thiết lập chiều cao: 95% màn hình hoặc wrap_content
        layoutParams.height = if (isHeightMatchParent) (height * 0.95).toInt() else WRAP_CONTENT

        // Thiết lập vị trí hiển thị
        layoutParams.gravity = layoutGravity
        if (layoutGravity != GRAVITY_CENTER) layoutParams.y = 100                    // margin theo chiều gravity
        this.window?.attributes = layoutParams

        super.onCreate(savedInstanceState)
    }

    /**
     * Quản lý lifecycle cho dialog để hỗ trợ LiveData và các thành phần lifecycle khác
     */
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    /**
     * Cập nhật trạng thái lifecycle khi dialog hiển thị
     */
    override fun show() {
        super.show()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }


    /**
     * Cập nhật trạng thái lifecycle khi dialog đóng
     */
    override fun dismiss() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.dismiss()
    }

    /**
     * Interface định nghĩa các phương thức cấu hình cho Builder pattern
     */
    interface IBuilder<VB: ViewBinding>{
        var isBorderRadius: Boolean
        var isWidthMatchParent: Boolean
        var isHeightMatchParent: Boolean
        var isTransparent: Boolean
        var layoutGravity: Int

        fun isBorderRadius(isBorderRadius: Boolean): Builder<VB>
        fun isWidthMatchParent(isWidthMatchParent: Boolean): Builder<VB>
        fun isHeightMatchParent(isHeightMatchParent: Boolean): Builder<VB>
        fun isTransparent(isTransparent: Boolean): Builder<VB>
        fun layoutGravity(gravity: Int): Builder<VB>
        fun build(): AppBaseDialog<VB>
    }

    /**
     * Builder class để xây dựng và cấu hình dialog theo pattern Builder
     * Cho phép thiết lập các thuộc tính của dialog theo chuỗi phương thức
     */
    class Builder<VB: ViewBinding>(private val context: Context, val binding: VB) : IBuilder<VB>{
        // Các giá trị mặc định
        override var isBorderRadius: Boolean = false
        override var isWidthMatchParent: Boolean = true
        override var isHeightMatchParent: Boolean = false
        override var isTransparent: Boolean = true
        override var layoutGravity: Int = GRAVITY_CENTER

        //Thiết lâp bo góc cho dialog
        override fun isBorderRadius(isBorderRadius: Boolean): Builder<VB> {
            this.isBorderRadius = isBorderRadius
            return this
        }
        // Thiết lập chiều rộng của dialog
        override fun isWidthMatchParent(isWidthMatchParent: Boolean): Builder<VB> {
            this.isWidthMatchParent = isWidthMatchParent
            return this
        }
        // Thiết lập chiều cao của dialog
        override fun isHeightMatchParent(isHeightMatchParent: Boolean): Builder<VB> {
            this.isHeightMatchParent = isHeightMatchParent
            return this
        }
        // Thiết lập nền trong suốt cho dialog
        override fun isTransparent(isTransparent: Boolean): Builder<VB> {
            this.isTransparent = isTransparent
            return this
        }
        // Thiết lập vị trí hiển thị của dialog
        override fun layoutGravity(layoutGravity: Int): Builder<VB> {
            this.layoutGravity = layoutGravity
            return this
        }
        // Xây dựng dialog với các thuộc tính đã cấu hình
        override fun build(): AppBaseDialog<VB> = AppBaseDialog(
            context,
            binding,
            isBorderRadius,
            isTransparent,
            isWidthMatchParent,
            isHeightMatchParent,
            layoutGravity,
        )
    }
}

