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

abstract class AppBaseFragment <VB: ViewBinding> : Fragment(), HasScreenInjector
{

    protected val baseActivity: AppBaseActivity<*> by lazy {
        activity as AppBaseActivity<*>
    }

    private lateinit var screenComponent: AppComponent

    private var _binding: VB? = null
    protected val views: VB get() = _binding!!

    override fun onAttach(context: Context) {
        screenComponent = DaggerAppComponent.factory().create(context)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = getBinding(inflater, container)
        return views.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        uiDisposables.clear()
    }

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


    override fun injector(): AppComponent {
        return screenComponent
    }

    fun inValidate(){
    }

    abstract fun getBinding(inflater: LayoutInflater, container: ViewGroup?): VB

}