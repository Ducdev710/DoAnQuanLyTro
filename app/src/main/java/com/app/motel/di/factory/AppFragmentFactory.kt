package com.app.motel.di.factory

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import javax.inject.Inject
import javax.inject.Provider

/**
 * Factory tùy chỉnh để tạo các Fragment với dependency injection.
 *
 * Lớp này mở rộng FragmentFactory của Android để hỗ trợ việc tạo các Fragment
 * với các dependency được tiêm tự động thông qua Dagger.
 *
 * @property creators Map chứa các Provider cho từng lớp Fragment, được tiêm bởi Dagger
 */
class AppFragmentFactory @Inject constructor(
    private val creators: @JvmSuppressWildcards Map<Class<out Fragment>, Provider<Fragment>>
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        val fragmentClass = loadFragmentClass(classLoader, className)
        val creator: Provider<out Fragment>? = creators[fragmentClass]
        //return super.instantiate(classLoader, className)
        return if (creator == null) {
            Log.e("MotelFragmentFactory","Unknown model class: $className, fallback to default instance")
            super.instantiate(classLoader, className)
        } else {
            creator.get()
        }
    }

}