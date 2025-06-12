package com.app.motel.di.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Factory tùy chỉnh để tạo các ViewModel với dependency injection.
 *
 * Lớp này triển khai ViewModelProvider.Factory để hỗ trợ việc tạo các ViewModel
 * với các dependency được tiêm tự động thông qua Dagger.
 *
 * @property creators Map chứa các Provider cho từng lớp ViewModel, được tiêm bởi Dagger
 */
@Singleton
class AppViewModelFactory @Inject constructor(
    private val creators: @JvmSuppressWildcards Map<Class<out ViewModel>, Provider<ViewModel>>
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Tìm provider phù hợp cho modelClass
        val creator = creators[modelClass] ?: creators.entries.firstOrNull{
            // Nếu không tìm thấy chính xác, tìm kiếm lớp cha gần nhất
            modelClass.isAssignableFrom(it.key)
        }?.value ?: throw IllegalArgumentException("unknown model class $modelClass")

        // Tạo instance của ViewModel từ provider và ép kiểu về T
        return creator.get() as T
    }
}