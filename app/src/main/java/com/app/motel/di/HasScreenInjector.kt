package com.app.motel.di

/**
 * Interface đánh dấu các thành phần có khả năng cung cấp AppComponent.
 *
 * Interface này được sử dụng để xác định các thành phần trong ứng dụng
 * (thường là Activity hoặc Application) có thể cung cấp AppComponent
 * cho việc dependency injection.
 *
 * Điều này cho phép các Fragment hoặc các thành phần con khác có thể
 * truy cập AppComponent thông qua context của chúng mà không cần phải
 * truy cập trực tiếp vào Application.
 */
interface HasScreenInjector {

    fun injector(): AppComponent
}