package com.app.motel.core

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.app.motel.AppApplication
import com.app.motel.di.DaggerAppComponent
import com.app.motel.di.HasScreenInjector
import com.app.motel.di.AppComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Lớp Fragment cơ sở cho ứng dụng, tích hợp ViewBinding và Dependency Injection.
 * Cung cấp các chức năng chung và quản lý vòng đời cho tất cả các fragment.
 *
 * @param VB Kiểu ViewBinding được sử dụng để hiển thị nội dung Fragment
 */
abstract class AppBaseFragment <VB: ViewBinding> : Fragment(), HasScreenInjector
{
    /**
     * Tham chiếu đến Activity gốc đã được ép kiểu thành AppBaseActivity
     * Cho phép truy cập các phương thức của Activity từ Fragment
     */
    protected val baseActivity: AppBaseActivity<*> by lazy {
        activity as AppBaseActivity<*>
    }
    /**
     * Component DI cho Fragment, cung cấp các dependency cần thiết
     */
    private lateinit var screenComponent: AppComponent

    private var _binding: VB? = null
    protected val views: VB get() = _binding!!

    override fun onAttach(context: Context) {
        screenComponent = DaggerAppComponent.factory().create(context)
        super.onAttach(context)
    }

    /**
     * Tạo và khởi tạo binding cho view của Fragment
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = getBinding(inflater, container)
        return views.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        uiDisposables.clear()
    }

    /**
     * Giải phóng tất cả các tài nguyên khi Fragment bị hủy
     */
    override fun onDestroy() {
        super.onDestroy()
        uiDisposables.dispose()
    }

/* ==========================================================================================
 * Disposable
 * ========================================================================================== */

    private val uiDisposables = CompositeDisposable()

    protected fun Disposable.disposeOnDestroyView() {
        uiDisposables.add(this)
    }

    /**
     * Hàm mở rộng cho ViewModel để quan sát các sự kiện và xử lý trên main thread
     * Tự động quản lý vòng đời subscription theo Fragment
     *
     * @param observer Hàm xử lý sự kiện
     */
    protected fun <T : AppViewEvent> AppBaseViewModel<*, *, T>.observeViewEvents(observer: (T) -> Unit) {
        viewEvents
            .observe()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                observer(it)
            }
            .disposeOnDestroyView()
    }

    protected fun <L: AppViewLiveData> AppBaseViewModel<L, *, *>.observerLivedata() = liveData

    /**
     * Triển khai phương thức từ interface HasScreenInjector
     * Trả về component DI để sử dụng trong fragment hoặc các child view
     */
    override fun injector(): AppComponent {
        return screenComponent
    }

    fun inValidate(){
    }

    /**
     * Phương thức trừu tượng để khởi tạo ViewBinding
     * Các lớp con bắt buộc phải triển khai phương thức này
     */
    abstract fun getBinding(inflater: LayoutInflater, container: ViewGroup?): VB

}