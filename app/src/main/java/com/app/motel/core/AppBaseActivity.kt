package com.app.motel.core

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.app.motel.di.HasScreenInjector
import com.app.motel.di.AppComponent
import com.app.motel.di.DaggerAppComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.launch

abstract class AppBaseActivity <VB : ViewBinding> : AppCompatActivity(), HasScreenInjector
{

    protected lateinit var views: VB


    private lateinit var appComponent: AppComponent

    private lateinit var fragmentFactory: FragmentFactory
    private lateinit var viewModelFactory: ViewModelProvider.Factory

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent = DaggerAppComponent.factory().create(this)
        fragmentFactory = appComponent.fragmentFactory()
        viewModelFactory = appComponent.viewModelFactory()

        supportFragmentManager.fragmentFactory = fragmentFactory  //giúp quản lý vòng đời của Fragment trong khi khôi phục trạng thái của activity và đảm bảo rằng việc tái tạo Fragment diễn ra đúng cách  ||||  Việc xác định FragmentFactory trong supportFragmentManager giúp đảm bảo rằng FragmentManager sẽ sử dụng FragmentFactory đã cung cấp để tái tạo các Fragment, đảm bảo rằng các tham số của Fragment như arguments hay dữ liệu trạng thái được giữ nguyên đúng cách.

        super.onCreate(savedInstanceState)
        views = getBinding()
        setContentView(views.root)



    }

    abstract fun getBinding(): VB



    @SuppressLint("CheckResult")
    protected fun <T : AppViewEvent> AppBaseViewModel<*, *, T>.observeViewEvents(observer: (T?) -> Unit) {
        viewEvents
            .observe()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                observer(it)
            }
    }

    protected fun <L: AppViewLiveData> AppBaseViewModel<L, *, *>.liveData() = liveData
    /**
     * use when create instance of fragment
     */
    protected fun createFragment(fragmentClass: Class<out Fragment>, args: Bundle?): Fragment {
        return fragmentFactory.instantiate(classLoader, fragmentClass.name).apply {
            arguments = args
        }
    }

    /**
     * use when create viewModel
     */
    protected val viewModelProvider
        get() = ViewModelProvider(this, viewModelFactory)


    override fun injector(): AppComponent {
        return appComponent
    }

}