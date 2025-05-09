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

abstract class AppBaseBottomSheet<VB: ViewBinding> : BottomSheetDialogFragment() {

    abstract fun getBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    private var _binding: VB? = null
    protected val views: VB get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isBorderRadiusTop) setStyle(STYLE_NORMAL, R.style.BorderRadiusBottomSheetDialogTheme)
        else setStyle(STYLE_NORMAL, R.style.UnBorderRadiusBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = getBinding(inflater, container)
        return views.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStateBottom()
    }

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

    private fun setStateBottom(){
        val bottomSheetDialog: BottomSheetDialog = dialog as BottomSheetDialog
        val behavior = bottomSheetDialog.behavior
        behavior.isDraggable = isDraggable
        behavior.state = if (isExpanded) STATE_EXPANDED else STATE_COLLAPSED
    }
}
