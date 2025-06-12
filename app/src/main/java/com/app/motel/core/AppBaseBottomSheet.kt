package com.app.motel.core
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.app.motel.R
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Lớp cơ sở cho Bottom Sheet Dialog sử dụng ViewBinding.
 * Cung cấp các chức năng và cấu hình cơ bản cho các Bottom Sheet trong ứng dụng.
 *
 * @param VB Kiểu ViewBinding được sử dụng để hiển thị nội dung Bottom Sheet
 */
abstract class AppBaseBottomSheet<VB: ViewBinding> : BottomSheetDialogFragment() {

    abstract fun getBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    /**
     * Biến binding tạm thời, được khởi tạo trong onCreateView và xóa trong onDestroyView.
     */
    private var _binding: VB? = null

    /**
     * Thuộc tính để truy cập binding đã được khởi tạo.
     * Ném ngoại lệ nếu được truy cập trước khi khởi tạo hoặc sau khi hủy.
     */
    protected val views: VB get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Thiết lập style cho bottom sheet dựa vào cấu hình bo góc
        if (isBorderRadiusTop) setStyle(STYLE_NORMAL, R.style.BorderRadiusBottomSheetDialogTheme)
        else setStyle(STYLE_NORMAL, R.style.UnBorderRadiusBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Khởi tạo binding
        _binding = getBinding(inflater, container)
        return views.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStateBottom()
    }


    /**
     * Phương thức tiện ích để đăng ký observer cho các sự kiện từ ViewModel.
     * Sử dụng RxJava để quan sát sự kiện và thực thi trên main thread.
     *
     * @param observer Hàm xử lý sự kiện
     */
    @SuppressLint("CheckResult")
    protected fun <T : AppViewEvent> AppBaseViewModel<*, *, T>.observeViewEvents(observer: (T) -> Unit) {
        viewEvents
            .observe()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                observer(it)
            }
    }

    open val isBorderRadiusTop: Boolean = true  // bo góc trên hay k
    open val isDraggable: Boolean = true        // vuốt xuống bottom hay k
    open val isExpanded: Boolean = false        // rộng màn hình hay k, RelativeLayout với được

    /**
     * Thiết lập trạng thái ban đầu cho bottom sheet dựa trên các cấu hình.
     */
    private fun setStateBottom(){
        val bottomSheetDialog: BottomSheetDialog = dialog as BottomSheetDialog
        val behavior = bottomSheetDialog.behavior
        behavior.isDraggable = isDraggable
        behavior.state = if (isExpanded) STATE_EXPANDED else STATE_COLLAPSED
    }
    /**
     * Xóa tham chiếu đến binding khi view bị hủy để tránh rò rỉ bộ nhớ.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
